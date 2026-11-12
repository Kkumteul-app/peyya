package com.peyya.app.data

import android.util.Log
import com.peyya.app.BuildConfig
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.PlaceCategory
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 국토교통부 브이월드 장소 검색.
 * 집/기준점 주변으로 우선 정렬·필터해서 전국 줌이 되지 않게 한다.
 */
object VWorldPlaceSearch {
    private const val TAG = "VWorldPlaceSearch"
    private const val ENDPOINT = "https://api.vworld.kr/req/search"

    /** 수도권 대략 bbox (lon,lat) — 상세 영역 검색용 */
    private const val METRO_BBOX = "126.60,37.25,127.35,37.85"
    private const val LOCAL_RADIUS_M = 80_000

    fun hasApiKey(): Boolean = BuildConfig.VWORLD_API_KEY.isNotBlank()

    fun search(
        query: String,
        nearLat: Double = MapSearchCatalog.home.latitude,
        nearLng: Double = MapSearchCatalog.home.longitude,
    ): SearchOutcome {
        val q = query.trim()
        if (q.isEmpty()) {
            return SearchOutcome(
                places = emptyList(),
                sourceLabel = "검색어를 입력하세요",
                fromNetwork = false,
            )
        }
        if (!hasApiKey()) {
            return SearchOutcome(
                places = rankLocal(MapSearchCatalog.search(q), q, nearLat, nearLng),
                sourceLabel = "오프라인 카탈로그 · VWorld 키 미설정",
                fromNetwork = false,
            )
        }
        return runCatching { searchRemote(q, nearLat, nearLng) }
            .onFailure { Log.w(TAG, "VWorld search failed, fallback", it) }
            .getOrElse {
                SearchOutcome(
                    places = rankLocal(MapSearchCatalog.search(q), q, nearLat, nearLng),
                    sourceLabel = "오프라인 카탈로그 · 네트워크 폴백",
                    fromNetwork = false,
                )
            }
    }

    private fun searchRemote(query: String, nearLat: Double, nearLng: Double): SearchOutcome {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        // 1차: 수도권 bbox로 상세 검색
        val metro = fetchPlaces(
            "$ENDPOINT?service=search&request=search&version=2.0" +
                "&crs=EPSG:4326&bbox=$METRO_BBOX&size=10&page=1&type=place" +
                "&format=json&errorformat=json&key=${BuildConfig.VWORLD_API_KEY}&query=$encoded",
        )
        val rankedMetro = rankLocal(metro, query, nearLat, nearLng)
        if (rankedMetro.isNotEmpty()) {
            return SearchOutcome(
                places = rankedMetro,
                sourceLabel = "국토교통부 브이월드 · 주변 ${rankedMetro.size}건",
                fromNetwork = true,
            )
        }
        // 2차: 전국 검색 후 가까운 곳만 / 최유사 1건
        val nationwide = fetchPlaces(
            "$ENDPOINT?service=search&request=search&version=2.0" +
                "&crs=EPSG:4326&size=12&page=1&type=place" +
                "&format=json&errorformat=json&key=${BuildConfig.VWORLD_API_KEY}&query=$encoded",
        )
        val ranked = rankLocal(nationwide, query, nearLat, nearLng)
        return SearchOutcome(
            places = ranked.ifEmpty { MapSearchCatalog.search(query) },
            sourceLabel = if (ranked.isEmpty()) {
                "브이월드 0건 · 오프라인 보조"
            } else {
                "국토교통부 브이월드 · 상세 ${ranked.size}건"
            },
            fromNetwork = true,
        )
    }

    private fun fetchPlaces(urlString: String): List<MapPlace> {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 6_000
            readTimeout = 6_000
            requestMethod = "GET"
        }
        try {
            val body = conn.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            val response = JSONObject(body).getJSONObject("response")
            if (response.optString("status") != "OK") return emptyList()
            val items = response.getJSONObject("result").getJSONArray("items")
            val places = mutableListOf<MapPlace>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val title = item.optString("title")
                    .replace(Regex("</?[^>]+>"), "")
                    .trim()
                if (title.isEmpty()) continue
                val point = item.optJSONObject("point") ?: continue
                val lng = point.optString("x").toDoubleOrNull() ?: continue
                val lat = point.optString("y").toDoubleOrNull() ?: continue
                val address = item.optJSONObject("address")
                val road = address?.optString("road").orEmpty()
                val parcel = address?.optString("parcel").orEmpty()
                val addr = road.ifBlank { parcel }.ifBlank { "국토부 공간정보" }
                val id = "vworld-" + item.optString("id").ifBlank { "${lat}_${lng}" }
                places += MapPlace(
                    id = id,
                    name = title,
                    address = addr,
                    latitude = lat,
                    longitude = lng,
                    photoAsset = DemoContent.assetPath(
                        when {
                            title.contains("박물관") || title.contains("기념") -> "museum.jpg"
                            title.contains("공원") || title.contains("산") -> "park.jpg"
                            title.contains("카페") -> "cafe.jpg"
                            title.contains("식당") || title.contains("맛집") -> "food.jpg"
                            else -> "road.jpg"
                        },
                    ),
                    category = when {
                        title.contains("박물관") || title.contains("미술관") -> PlaceCategory.CULTURE
                        title.contains("공원") -> PlaceCategory.OUTDOOR
                        title.contains("카페") -> PlaceCategory.CAFE
                        title.contains("식당") || title.contains("맛집") -> PlaceCategory.FOOD
                        else -> PlaceCategory.ANCHOR
                    },
                )
            }
            return places
        } finally {
            conn.disconnect()
        }
    }

    /** 이름 유사도 + 거리 우선, 너무 먼 전국 결과는 제외(또는 최단 1건만). */
    private fun rankLocal(
        places: List<MapPlace>,
        query: String,
        nearLat: Double,
        nearLng: Double,
    ): List<MapPlace> {
        if (places.isEmpty()) return emptyList()
        val q = query.trim()
        val scored = places.map { place ->
            val dist = haversineMeters(nearLat, nearLng, place.latitude, place.longitude)
            val nameScore = when {
                place.name == q -> 0
                place.name.contains(q) -> 1
                q.contains(place.name.take(4)) -> 2
                else -> 3
            }
            Triple(place.copy(distanceMeters = dist), nameScore, dist)
        }.sortedWith(compareBy({ it.second }, { it.third }))

        val local = scored.filter { it.third <= LOCAL_RADIUS_M }.map { it.first }
        if (local.isNotEmpty()) return local.take(8)

        // 전국만 있으면 가장 가까운(또는 이름 최일치) 1~2건만 → 상세 줌
        return scored.take(2).map { it.first }
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * r * asin(min(1.0, sqrt(a)))).toInt()
    }

    data class SearchOutcome(
        val places: List<MapPlace>,
        val sourceLabel: String,
        val fromNetwork: Boolean,
    )
}
