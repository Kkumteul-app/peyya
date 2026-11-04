package com.peyya.app.adapt

/**
 * 당일 적응 제안.
 * 혼잡 시 우회 후보를 주고, 본 목적지는 [keepInRevisitQueue]로 유지한다.
 */
data class AdaptSuggestion(
    val originalStopId: String,
    val keepInRevisitQueue: Boolean,
    val detours: List<AdaptCandidate>,
    val parking: List<AdaptCandidate>,
    val congestion: CongestionSnapshot,
    val isDemoEstimate: Boolean,
)
