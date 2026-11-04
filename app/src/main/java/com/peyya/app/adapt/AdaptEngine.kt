package com.peyya.app.adapt

/**
 * 당일 적응 규칙 엔진 (순수).
 *
 * - Medium/High 혼잡 → 우회 후보 제안, 본 목적지는 재방문 큐 유지
 * - 주차는 도착 맥락에서 후보 제공
 * - 시연용 플래그는 스냅샷·후보·제안 모두에 전파
 */
object AdaptEngine {
    /** 혼잡도가 우회를 유발하는지. */
    fun needsDetour(level: CongestionLevel): Boolean =
        level == CongestionLevel.Medium || level == CongestionLevel.High

    fun suggest(
        stopId: String,
        congestion: CongestionSnapshot = AdaptMockCatalog.congestionByStopId[stopId]
            ?: CongestionSnapshot(stopId, CongestionLevel.Low, isDemoEstimate = true),
        detours: List<AdaptCandidate> = AdaptMockCatalog.detoursFor(stopId),
        parking: List<AdaptCandidate> = AdaptMockCatalog.parkingFor(stopId),
    ): AdaptSuggestion {
        val offerDetour = needsDetour(congestion.level)
        val selectedDetours = if (offerDetour) detours else emptyList()
        val demo = congestion.isDemoEstimate ||
            selectedDetours.any { it.isDemoEstimate } ||
            parking.any { it.isDemoEstimate }

        return AdaptSuggestion(
            originalStopId = stopId,
            keepInRevisitQueue = offerDetour,
            detours = selectedDetours,
            parking = parking,
            congestion = congestion,
            isDemoEstimate = demo,
        )
    }
}
