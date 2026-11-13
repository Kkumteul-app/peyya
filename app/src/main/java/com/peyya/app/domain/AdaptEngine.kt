package com.peyya.app.domain

data class AdaptSuggestion(
    val id: String,
    val kind: Kind,
    val title: String,
    val body: String,
    val demoEstimated: Boolean = true,
    val altStop: Stop? = null,
) {
    enum class Kind { Congestion, Detour, Parking }
}

object AdaptMockCatalog {
    fun forStop(stopId: String): List<AdaptSuggestion> = when (stopId) {
        "museum" -> listOf(
            AdaptSuggestion(
                id = "museum-crowd",
                kind = AdaptSuggestion.Kind.Congestion,
                title = "혼잡 · 시연용/추정",
                body = "주말 오후 대기 길어질 수 있어요. 실내 카페 우회 후 재방문도 가능합니다.",
            ),
            AdaptSuggestion(
                id = "museum-park",
                kind = AdaptSuggestion.Kind.Parking,
                title = "주차 후보 · 시연용/추정",
                body = "서빙고로 방면 주차 여유 구간을 우선 안내합니다.",
                altStop = Stop(
                    id = "museum-parking",
                    name = "국립중앙박물관 주차 (데모)",
                    address = "서울 용산구 서빙고로 137 주차장",
                    latitude = 37.52440,
                    longitude = 126.98010,
                    poiId = "peyya-demo-parking",
                ),
            ),
        )
        else -> listOf(
            AdaptSuggestion(
                id = "generic-detour",
                kind = AdaptSuggestion.Kind.Detour,
                title = "우회 스팟 · 시연용/추정",
                body = "웨이팅이 길면 가까운 짧은 스톱으로 잠시 우회할 수 있어요.",
            ),
        )
    }
}

object AdaptEngine {
    fun suggestionsOnArrive(stopId: String): List<AdaptSuggestion> =
        AdaptMockCatalog.forStop(stopId)
}
