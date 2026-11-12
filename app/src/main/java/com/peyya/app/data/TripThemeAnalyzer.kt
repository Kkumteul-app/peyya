package com.peyya.app.data

import com.peyya.app.domain.Stop
import com.peyya.app.viewmodel.PlanCompanion
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 경로를 한 문장 주제로 요약하고, 구간별 거리·시간을 추정한다.
 */
object TripThemeAnalyzer {
    data class LegEta(
        val distanceLabel: String,
        val durationLabel: String,
        val durationMinutes: Int = 0,
    ) {
        val combined: String get() = "$durationLabel · $distanceLabel"
    }

    fun themeSentence(
        stops: List<Stop>,
        companion: PlanCompanion = PlanCompanion.Family,
    ): String {
        val visits = stops.filterNot { it.id == MapSearchCatalog.home.id || it.name == "집" }
        if (visits.isEmpty()) return "오늘 떠날 코스를 준비 중이에요."

        val who = when (companion) {
            PlanCompanion.Family -> "가족과 함께"
            PlanCompanion.Solo -> "혼자"
            PlanCompanion.Friends -> "가까운 사람과"
        }
        val kinds = visits.map { classify(it) }.distinct()
        val placeHint = when {
            kinds.contains(Kind.Culture) && kinds.contains(Kind.Food) ->
                "박물관을 둘러보고 근처에서 식사하는"
            kinds.contains(Kind.Culture) && kinds.contains(Kind.Cafe) ->
                "문화 공간을 보고 카페에서 쉬는"
            kinds.contains(Kind.Outdoor) && kinds.contains(Kind.Food) ->
                "야외를 거닐고 맛집에서 마무리하는"
            kinds.contains(Kind.Culture) ->
                "문화·전시 중심으로 다녀오는"
            kinds.contains(Kind.Food) ->
                "맛집 코스로 즐기는"
            kinds.contains(Kind.Outdoor) ->
                "야외 산책 위주로 다녀오는"
            else ->
                "${visits.first().name}부터 ${visits.last().name}까지 이어가는"
        }
        return "$who $placeHint 하루예요."
    }

    /** 현재 목적지까지(직전 스톱→현재) 추정 ETA */
    fun legEta(from: Stop?, to: Stop): LegEta {
        val origin = from ?: return LegEta("—", "—")
        val km = haversineKm(origin, to).coerceAtLeast(0.3)
        val minutes = (km * 2.4 + 4).roundToInt().coerceIn(5, 120)
        val distanceLabel = if (km < 1.0) {
            "${(km * 1000).roundToInt()}m"
        } else {
            String.format(Locale.US, "%.1fkm", km)
        }
        return LegEta(
            distanceLabel = distanceLabel,
            durationLabel = "${minutes}분",
            durationMinutes = minutes,
        )
    }

    /** 전체 코스(첫 방문지→마지막) 합산 ETA */
    fun totalEta(selected: List<Stop>): LegEta {
        if (selected.isEmpty()) return LegEta("—", "—")
        if (selected.size == 1) {
            return legEta(MapSearchCatalog.home.toStop(), selected.first())
        }
        var km = 0.0
        for (i in 0 until selected.lastIndex) {
            km += haversineKm(selected[i], selected[i + 1])
        }
        val minutes = (km * 2.4 + selected.size * 3).roundToInt().coerceIn(5, 240)
        val distanceLabel = if (km < 1.0) {
            "${(km * 1000).roundToInt()}m"
        } else {
            String.format(Locale.US, "%.1fkm", km)
        }
        return LegEta(
            distanceLabel = distanceLabel,
            durationLabel = "${minutes}분",
            durationMinutes = minutes,
        )
    }

    /** index 스톱으로 가는 구간 ETA (직전 스톱 또는 집 기준) */
    fun etaIntoStop(selected: List<Stop>, index: Int): LegEta? {
        if (index !in selected.indices) return null
        val to = selected[index]
        val from = selected.getOrNull(index - 1) ?: MapSearchCatalog.home.toStop()
        return legEta(from, to)
    }

    private enum class Kind { Culture, Food, Cafe, Outdoor, Other }

    private fun classify(stop: Stop): Kind {
        val n = stop.name
        return when {
            n.contains("박물관") || n.contains("기념관") || n.contains("전시") || n.contains("미술관") -> Kind.Culture
            n.contains("카페") || n.contains("브런치") -> Kind.Cafe
            n.contains("한식") || n.contains("맛집") || n.contains("식당") || n.contains("담소") ||
                n.contains("국밥") || n.contains("고기") -> Kind.Food
            n.contains("공원") || n.contains("한강") || n.contains("타워") || n.contains("산책") -> Kind.Outdoor
            else -> Kind.Other
        }
    }

    private fun haversineKm(a: Stop, b: Stop): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * asin(min(1.0, sqrt(h)))
    }
}
