package com.peyya.app.data

import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.OpeningHours
import com.peyya.app.domain.PlaceCategory
import java.util.Calendar
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 시연용 지도 검색 카탈로그.
 * 한 곳을 검색하면 주변 맛집·볼거리·추천을 영역별로 돌려준다.
 */
object MapSearchCatalog {
    private val photo = DemoContent::assetPath

    /** 경로 출발 기본값 (집) */
    val home: MapPlace = MapPlace(
        id = "home",
        name = "집",
        address = "안양역 푸르지오더샵아파트",
        latitude = 37.40150,
        longitude = 126.92260,
        photoAsset = photo("road.jpg"),
        category = PlaceCategory.ANCHOR,
    )

    val searchablePlaces: List<MapPlace> = listOf(
        MapPlace(
            id = "museum",
            name = "국립중앙박물관",
            address = "서울 용산구 서빙고로 137",
            latitude = 37.52386,
            longitude = 126.98047,
            photoAsset = photo("museum.jpg"),
            category = PlaceCategory.CULTURE,
            needsBooking = true,
            bookingUrl = PresetTrips.MUSEUM_BOOKING_URL,
            prepChecklist = PresetTrips.MUSEUM_PREP_CHECKLIST,
            highlights = DestinationTipsCatalog.museumTips.highlights,
            parkingTips = DestinationTipsCatalog.museumTips.parkingTips,
            familyTips = DestinationTipsCatalog.museumTips.familyTips,
            openingHours = OpeningHours.daily(
                openHour = 10,
                closeHour = 18,
                closedWeekdays = setOf(Calendar.MONDAY),
                noteKo = "월요일 휴관",
            ),
        ),
        MapPlace(
            id = "yongsan-station",
            name = "용산역",
            address = "서울 용산구 한강대로 405",
            latitude = 37.52985,
            longitude = 126.96480,
            photoAsset = photo("road.jpg"),
            category = PlaceCategory.ANCHOR,
        ),
        MapPlace(
            id = "namsan",
            name = "남산서울타워",
            address = "서울 용산구 남산공원길 105",
            latitude = 37.55117,
            longitude = 126.98823,
            photoAsset = photo("park.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
        MapPlace(
            id = "hangang",
            name = "한강공원 이촌지구",
            address = "서울 용산구 이촌로 72",
            latitude = 37.51820,
            longitude = 126.97050,
            photoAsset = photo("park.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
    )

    /** 자주 가는 곳 시연 */
    val frequentPlaces: List<MapPlace> = listOf(
        home,
        searchablePlaces.first { it.id == "museum" },
        searchablePlaces.first { it.id == "yongsan-station" },
    )

    private val foodPool: List<MapPlace> = listOf(
        MapPlace(
            id = "restaurant",
            name = "용산 한식 담소",
            address = "서울 용산구 한강대로 405 인근",
            latitude = 37.53120,
            longitude = 126.96580,
            photoAsset = photo("food.jpg"),
            category = PlaceCategory.FOOD,
            strollerOk = true,
            highChairOk = true,
            openingHours = OpeningHours.daily(11, 21),
        ),
        MapPlace(
            id = "food-yongsan-bbq",
            name = "용산 고기집 골목",
            address = "서울 용산구 청파로",
            latitude = 37.53210,
            longitude = 126.96890,
            photoAsset = photo("food.jpg"),
            category = PlaceCategory.FOOD,
            strollerOk = false,
            highChairOk = false,
            openingHours = OpeningHours.daily(12, 22),
        ),
        MapPlace(
            id = "cafe-hannam",
            name = "한남 카페 골목",
            address = "서울 용산구 이태원로 인근",
            latitude = 37.53450,
            longitude = 126.99420,
            photoAsset = photo("cafe.jpg"),
            category = PlaceCategory.CAFE,
            strollerOk = true,
            highChairOk = false,
            openingHours = OpeningHours.daily(9, 22),
        ),
        MapPlace(
            id = "cafe-ichon",
            name = "이촌 브런치 카페",
            address = "서울 용산구 이촌로",
            latitude = 37.52180,
            longitude = 126.97320,
            photoAsset = photo("cafe.jpg"),
            category = PlaceCategory.CAFE,
            strollerOk = true,
            highChairOk = true,
            openingHours = OpeningHours.daily(9, 20),
        ),
    )

    private val sightPool: List<MapPlace> = listOf(
        MapPlace(
            id = "culture-war-memorial",
            name = "전쟁기념관",
            address = "서울 용산구 이태원로 29",
            latitude = 37.53655,
            longitude = 126.97700,
            photoAsset = photo("museum.jpg"),
            category = PlaceCategory.CULTURE,
            openingHours = OpeningHours.daily(
                openHour = 9,
                closeHour = 18,
                closedWeekdays = setOf(Calendar.MONDAY),
                noteKo = "월요일 휴관",
            ),
        ),
        MapPlace(
            id = "culture-yongsan-gallery",
            name = "용산 아트갤러리",
            address = "서울 용산구 한강대로",
            latitude = 37.52890,
            longitude = 126.96840,
            photoAsset = photo("museum.jpg"),
            category = PlaceCategory.CULTURE,
            openingHours = OpeningHours.daily(10, 20, noteKo = "연중무휴"),
        ),
        MapPlace(
            id = "namsan-near",
            name = "남산서울타워",
            address = "서울 용산구 남산공원길 105",
            latitude = 37.55117,
            longitude = 126.98823,
            photoAsset = photo("park.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
        MapPlace(
            id = "hangang-near",
            name = "한강공원 이촌지구",
            address = "서울 용산구 이촌로 72",
            latitude = 37.51820,
            longitude = 126.97050,
            photoAsset = photo("park.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
    )

    private val recommendPool: List<MapPlace> = listOf(
        MapPlace(
            id = "recommend-family",
            name = "용산가족공원",
            address = "서울 용산구 서빙고로",
            latitude = 37.52240,
            longitude = 126.98310,
            photoAsset = photo("park.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
        MapPlace(
            id = "recommend-market",
            name = "이태원 거리 산책",
            address = "서울 용산구 이태원로",
            latitude = 37.53480,
            longitude = 126.99450,
            photoAsset = photo("road.jpg"),
            category = PlaceCategory.OUTDOOR,
        ),
    )

    data class NearbyBuckets(
        val food: List<MapPlace>,
        val sights: List<MapPlace>,
        val recommended: List<MapPlace>,
    ) {
        fun all(): List<MapPlace> = food + sights + recommended
    }

    fun search(query: String): List<MapPlace> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return searchablePlaces.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.address.contains(q, ignoreCase = true) ||
                it.category.labelKo.contains(q)
        }
    }

    fun recentSearches(): List<MapPlace> = searchablePlaces.take(3)

    fun hasYoungChild(family: FamilyProfile?): Boolean =
        family?.members?.any { it.ageBand == AgeBand.CHILD } == true

    /** 동행이 등록 가족이고 아이가 있을 때만 키즈 편의 우선 */
    fun preferKidAmenities(
        family: FamilyProfile?,
        companionIsFamily: Boolean = true,
    ): Boolean = companionIsFamily && hasYoungChild(family)

    /**
     * 선택한 목적지(앵커) 주변 추천.
     * 카탈로그에 가까운 POI가 있으면 그걸 쓰고, 없으면 앵커 좌표 기준으로 로컬 후보를 생성한다.
     * 아이가 있으면 유모차·아기의자 가능 장소를 우선한다.
     */
    fun nearbyBuckets(
        anchor: MapPlace,
        family: FamilyProfile? = null,
        companionIsFamily: Boolean = true,
    ): NearbyBuckets {
        val withKids = preferKidAmenities(family, companionIsFamily)
        val localFood = rankedNear(
            anchor = anchor,
            pool = foodPool,
            limit = 3,
            maxMeters = LOCAL_NEAR_METERS,
            preferKidFriendly = withKids,
        )
        val localSights = rankedNear(anchor, sightPool, limit = 3, maxMeters = LOCAL_NEAR_METERS)
        val localRec = rankedNear(anchor, recommendPool, limit = 2, maxMeters = LOCAL_NEAR_METERS)

        val foodTemplates = if (withKids) kidFoodTemplates else defaultFoodTemplates
        return NearbyBuckets(
            food = localFood.ifEmpty {
                synthesizeAround(anchor, foodTemplates, PlaceCategory.FOOD, kidDefault = withKids)
            },
            sights = localSights.ifEmpty { synthesizeAround(anchor, sightTemplates, PlaceCategory.OUTDOOR) },
            recommended = localRec.ifEmpty {
                synthesizeAround(anchor, recommendTemplates, PlaceCategory.OUTDOOR, limit = 2)
            },
        )
    }

    /** 하위 호환 — 평면 리스트 */
    fun nearbyOf(anchor: MapPlace, limit: Int = 4): List<MapPlace> =
        nearbyBuckets(anchor).all().take(limit)

    private fun rankedNear(
        anchor: MapPlace,
        pool: List<MapPlace>,
        limit: Int,
        maxMeters: Int,
        preferKidFriendly: Boolean = false,
    ): List<MapPlace> = pool
        .asSequence()
        .filter { it.id != anchor.id && it.name != anchor.name }
        .map { it.copy(distanceMeters = haversineMeters(anchor, it)) }
        .filter { (it.distanceMeters ?: Int.MAX_VALUE) <= maxMeters }
        .sortedWith(
            compareBy<MapPlace> {
                if (preferKidFriendly) if (it.kidFriendly) 0 else 1 else 0
            }.thenBy { it.distanceMeters ?: Int.MAX_VALUE },
        )
        .take(limit)
        .toList()

    private data class NearbyTemplate(
        val idSuffix: String,
        val name: String,
        val addressSuffix: String,
        val dLat: Double,
        val dLng: Double,
        val photo: String,
        val category: PlaceCategory? = null,
        val strollerOk: Boolean = false,
        val highChairOk: Boolean = false,
    )

    private val defaultFoodTemplates = listOf(
        NearbyTemplate("food-a", "근처 한식당", "인근 맛집", 0.0045, 0.0030, "food.jpg", PlaceCategory.FOOD),
        NearbyTemplate("food-b", "근처 카페", "인근 카페", -0.0035, 0.0045, "cafe.jpg", PlaceCategory.CAFE),
        NearbyTemplate("food-c", "근처 분식·국밥", "인근 식사", 0.0025, -0.0040, "food.jpg", PlaceCategory.FOOD),
    )

    /** 아이 동반 시 — 유모차·아기의자 가능 후보 */
    private val kidFoodTemplates = listOf(
        NearbyTemplate(
            "food-kid-a", "근처 가족 한식당", "유모차·아기의자",
            0.0045, 0.0030, "food.jpg", PlaceCategory.FOOD,
            strollerOk = true, highChairOk = true,
        ),
        NearbyTemplate(
            "food-kid-b", "근처 키즈 카페", "유모차·아기의자",
            -0.0035, 0.0045, "cafe.jpg", PlaceCategory.CAFE,
            strollerOk = true, highChairOk = true,
        ),
        NearbyTemplate(
            "food-kid-c", "근처 가족석 식당", "아기의자",
            0.0025, -0.0040, "food.jpg", PlaceCategory.FOOD,
            strollerOk = true, highChairOk = true,
        ),
    )

    private val sightTemplates = listOf(
        NearbyTemplate("sight-a", "근처 공원", "인근 산책", -0.0050, 0.0020, "park.jpg"),
        NearbyTemplate("sight-b", "근처 문화공간", "인근 볼거리", 0.0030, -0.0035, "museum.jpg", PlaceCategory.CULTURE),
        NearbyTemplate("sight-c", "근처 전망·거리", "인근 명소", 0.0060, 0.0050, "park.jpg"),
    )

    private val recommendTemplates = listOf(
        NearbyTemplate("rec-a", "가족 추천 스팟", "인근 추천", -0.0020, -0.0025, "park.jpg"),
        NearbyTemplate("rec-b", "짧은 산책 코스", "인근 추천", 0.0040, -0.0015, "road.jpg"),
    )

    private fun synthesizeAround(
        anchor: MapPlace,
        templates: List<NearbyTemplate>,
        defaultCategory: PlaceCategory,
        limit: Int = templates.size,
        kidDefault: Boolean = false,
    ): List<MapPlace> {
        val area = areaLabel(anchor)
        return templates.take(limit).map { t ->
            val lat = anchor.latitude + t.dLat
            val lng = anchor.longitude + t.dLng
            val meters = haversineMeters(
                anchor,
                anchor.copy(latitude = lat, longitude = lng),
            )
            val shortName = t.name.removePrefix("근처 ").trim()
            MapPlace(
                id = "near-${anchor.id}-${t.idSuffix}",
                name = if (area.isNotBlank()) "$area $shortName" else t.name,
                address = "${anchor.name} 주변 · ${t.addressSuffix}",
                latitude = lat,
                longitude = lng,
                photoAsset = photo(t.photo),
                category = t.category ?: defaultCategory,
                distanceMeters = meters,
                strollerOk = t.strollerOk || kidDefault,
                highChairOk = t.highChairOk || kidDefault,
            )
        }
    }

    /** 주소/이름에서 동네 힌트 추출 */
    private fun areaLabel(anchor: MapPlace): String {
        val raw = "${anchor.address} ${anchor.name}"
        val m = Regex("""([가-힣]+(?:구|시|군|동|읍|면))""").findAll(raw).map { it.groupValues[1] }.toList()
        return m.firstOrNull { it.endsWith("구") || it.endsWith("시") }
            ?: m.firstOrNull()
            ?: ""
    }

    private fun haversineMeters(a: MapPlace, b: MapPlace): Int {
        val r = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return (2 * r * asin(min(1.0, sqrt(h)))).toInt()
    }

    private const val LOCAL_NEAR_METERS = 5_000
}
