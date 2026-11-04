package com.peyya.app.adapt

/**
 * 국립중앙박물관 인근 우회·주차 목데이터.
 * 시연용 — 실시간 API가 아니다.
 */
object AdaptMockCatalog {
    const val DEMO_LABEL: String = "시연용/추정"

    val museumDetours: List<AdaptCandidate> = listOf(
        AdaptCandidate(
            id = "detour-namsan-walk",
            name = "용산가족공원 산책",
            kind = AdaptKind.Detour,
            latitude = 37.52490,
            longitude = 126.98310,
            poiId = "peyya-demo-detour-park",
            address = "서울 용산구 서빙고로 137 인근",
            isDemoEstimate = true,
        ),
        AdaptCandidate(
            id = "detour-hangang-cafe",
            name = "이촌한강공원 카페 코너",
            kind = AdaptKind.Detour,
            latitude = 37.51780,
            longitude = 126.97020,
            poiId = "peyya-demo-detour-cafe",
            address = "서울 용산구 이촌로 인근",
            isDemoEstimate = true,
        ),
    )

    val museumParking: List<AdaptCandidate> = listOf(
        AdaptCandidate(
            id = "park-museum-main",
            name = "국립중앙박물관 주차장",
            kind = AdaptKind.Parking,
            latitude = 37.52440,
            longitude = 126.97980,
            poiId = "peyya-demo-park-museum",
            address = "서울 용산구 서빙고로 137",
            isDemoEstimate = true,
        ),
        AdaptCandidate(
            id = "park-yongsan-station",
            name = "용산역 공영주차장(추정)",
            kind = AdaptKind.Parking,
            latitude = 37.52990,
            longitude = 126.96480,
            poiId = "peyya-demo-park-yongsan",
            address = "서울 용산구 한강대로 405 인근",
            isDemoEstimate = true,
        ),
    )

    /** 스톱 id → 혼잡 목 스냅샷. */
    val congestionByStopId: Map<String, CongestionSnapshot> = mapOf(
        "museum" to CongestionSnapshot(
            stopId = "museum",
            level = CongestionLevel.High,
            waitMinutes = 45,
            isDemoEstimate = true,
        ),
        "restaurant" to CongestionSnapshot(
            stopId = "restaurant",
            level = CongestionLevel.Low,
            waitMinutes = 5,
            isDemoEstimate = true,
        ),
    )

    fun detoursFor(stopId: String): List<AdaptCandidate> =
        if (stopId == "museum") museumDetours else emptyList()

    fun parkingFor(stopId: String): List<AdaptCandidate> =
        if (stopId == "museum") museumParking else emptyList()
}
