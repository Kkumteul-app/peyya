package com.peyya.app.review

import com.peyya.app.domain.Stop

enum class ReviewUiStep {
    Intro,
    Rating,
    Submitted,
}

/**
 * Completed 이후 「오늘 하루는 어땠나요?」플로우 UI 상태.
 * [stops]는 세션 시작 시점 selected 스냅샷.
 */
data class ActiveReviewUi(
    val sessionId: String,
    val tripId: String,
    val tripTitle: String,
    val stops: List<Stop>,
    val step: ReviewUiStep = ReviewUiStep.Intro,
    val stopIndex: Int = 0,
    val collected: List<StopReview> = emptyList(),
    /** 0 = 미선택, 1..5 = 선택됨 */
    val draftStars: Int = 0,
    val draftComment: String = "",
) {
    val currentStop: Stop? = stops.getOrNull(stopIndex)

    val isLastStop: Boolean =
        stops.isNotEmpty() && stopIndex >= stops.lastIndex

    val progressLabel: String =
        if (stops.isEmpty()) "" else "장소 ${stopIndex + 1} / ${stops.size}"
}
