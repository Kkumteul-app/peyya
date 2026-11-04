package com.peyya.app.review

/**
 * 완료 여행([com.peyya.app.progress.OnTripCompleted]의 tripId)에 대한 리뷰 세션.
 */
data class TripReviewSession(
    val sessionId: String,
    val tripId: String,
    val tripTitle: String,
    val status: TripReviewSessionStatus,
    val stopReviews: List<StopReview>,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long? = null,
)
