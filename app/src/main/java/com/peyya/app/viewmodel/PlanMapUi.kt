package com.peyya.app.viewmodel

import com.peyya.app.data.MapSearchCatalog
import com.peyya.app.data.PresetTrips
import com.peyya.app.data.TripThemeAnalyzer
import com.peyya.app.data.VisitFeasibilityAnalyzer
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.TripPlan
import java.util.Calendar

/** 바로 출발할지, 주말 등으로 미리 담아둘지 */
enum class PlanTiming(val labelKo: String, val hintKo: String) {
    Now("바로 실행", "지금 이 경로로 출발"),
    Weekend("주말 계획", "이번 주말에 갈 코스로 저장"),
}

/** 누구와 갈지 — 추천(유모차·아기의자 등)에 반영 */
enum class PlanCompanion(val labelKo: String, val hintKo: String) {
    Family("등록 가족", "마이에 등록된 가족과"),
    Solo("혼자", "혼자 가는 코스"),
    Friends("연인·친구", "소규모 동행"),
}

/**
 * 계획 탭 — 지도 중심 검색 · 주변 영역 · 경로(출발/경유/도착).
 */
data class PlanMapUi(
    val query: String = "",
    val searchResults: List<MapPlace> = emptyList(),
    val searching: Boolean = false,
    /** 예: 국토교통부 브이월드 · 8건 */
    val searchSourceLabel: String = "",
    /** 검색으로 고른 기준 장소 (지도 중심) */
    val focus: MapPlace? = null,
    /**
     * 검색에서 고른 직후 — 종료할지/다음 경로를 더할지 확인 대기.
     * null이면 확인 UI 숨김.
     */
    val pendingPlace: MapPlace? = null,
    val nearbyFood: List<MapPlace> = emptyList(),
    val nearbySights: List<MapPlace> = emptyList(),
    val nearbyRecommended: List<MapPlace> = emptyList(),
    /** 경로 스톱 순서: 첫=출발, 끝=도착, 중간=경유 — 사용자가 수동으로 바꿀 수 있음 */
    val routeStops: List<MapPlace> = emptyList(),
    /** 길찾기 실행 여부 */
    val routeReady: Boolean = false,
    val routeSummary: String? = null,
    /** 바로 실행 vs 주말(예약) 계획 */
    val timing: PlanTiming = PlanTiming.Now,
    /** 주말 계획으로 저장됐는지 */
    val weekendSaved: Boolean = false,
    /** 등록 가족 / 혼자 / 연인·친구 */
    val companion: PlanCompanion = PlanCompanion.Family,
    /** 출발 예정 시각 (epoch ms) */
    val departAtEpochMs: Long = defaultDepartAtEpochMs(),
    /** 방문 가능 판정 결과 */
    val visitChecks: List<VisitFeasibilityAnalyzer.VisitCheck> = emptyList(),
    val visitSummaryKo: String? = null,
    /**
     * 누구와/언제/출발일시 사전 세팅 완료 여부.
     * false면 계획 전에 세팅 팝업을 먼저 띄운다.
     */
    val prefsReady: Boolean = false,
) {
    val routeIds: Set<String> get() = routeStops.map { it.id }.toSet()

    fun isOnRoute(placeId: String): Boolean = placeId in routeIds

    fun checkFor(placeId: String): VisitFeasibilityAnalyzer.VisitCheck? =
        visitChecks.firstOrNull { it.placeId == placeId }

    fun prefsSummaryKo(familyName: String): String {
        val who = when (companion) {
            PlanCompanion.Family -> familyName.ifBlank { PlanCompanion.Family.labelKo }
            else -> companion.labelKo
        }
        val whenLabel = timing.labelKo
        val depart = VisitFeasibilityAnalyzer.formatDepartLabel(departAtEpochMs)
        return "$who · $whenLabel · $depart"
    }

    companion object {
        fun defaultDepartAtEpochMs(now: Long = System.currentTimeMillis()): Long {
            // 다음 5분 단위로 올림
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            val m = cal.get(Calendar.MINUTE)
            val bump = ((m / 5) + 1) * 5
            if (bump >= 60) {
                cal.add(Calendar.HOUR_OF_DAY, 1)
                cal.set(Calendar.MINUTE, 0)
            } else {
                cal.set(Calendar.MINUTE, bump)
            }
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun seededForMuseumTrip(): PlanMapUi {
            val museum = MapSearchCatalog.searchablePlaces.first { it.id == "museum" }
            val buckets = MapSearchCatalog.nearbyBuckets(museum, PresetTrips.defaultFamily)
            val restaurant = buckets.food.firstOrNull { it.id == "restaurant" }
                ?: buckets.food.firstOrNull()
            val route = buildList {
                add(MapSearchCatalog.home)
                if (restaurant != null) add(restaurant)
                add(museum)
            }
            val depart = defaultDepartAtEpochMs()
            val report = VisitFeasibilityAnalyzer.evaluate(
                routeStops = route,
                departAtEpochMs = depart,
                alternativePool = buckets.all(),
            )
            return PlanMapUi(
                query = "",
                searchResults = emptyList(),
                focus = museum,
                nearbyFood = buckets.food,
                nearbySights = buckets.sights,
                nearbyRecommended = buckets.recommended,
                routeStops = route,
                routeReady = true,
                routeSummary = "약 48분 · 38.2km · 경유 ${route.size - 2}곳",
                departAtEpochMs = depart,
                visitChecks = report.checks,
                visitSummaryKo = report.summaryKo,
                prefsReady = false,
            )
        }
    }
}

fun PlanMapUi.toTripPlan(base: TripPlan): TripPlan {
    if (routeStops.isEmpty()) return base
    // 내비 실행용: 집(출발)은 제외하고 실제 방문지부터
    val visitStops = routeStops
        .filterNot { it.id == MapSearchCatalog.home.id }
        .ifEmpty { routeStops }
        .map { it.toStop(selected = true) }
    val title = TripThemeAnalyzer.themeSentence(visitStops, companion)
    return base.copy(title = title, stops = visitStops)
}
