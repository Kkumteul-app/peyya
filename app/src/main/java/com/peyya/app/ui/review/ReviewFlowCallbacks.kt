package com.peyya.app.ui.review

/** TripScreen → Phone/Car 리뷰·코스 콜백 묶음. */
data class ReviewFlowCallbacks(
    val onBeginReview: () -> Unit = {},
    val onSkipReviewSession: () -> Unit = {},
    val onSetReviewStars: (Int) -> Unit = {},
    val onSetReviewComment: (String) -> Unit = {},
    val onCommitReviewStop: () -> Unit = {},
    val onSkipReviewStop: () -> Unit = {},
    val onAcknowledgeReviewSubmitted: () -> Unit = {},
    val onApplyCourse: () -> Unit = {},
    val onDismissCourse: () -> Unit = {},
    val onAcknowledgeCourseApplied: () -> Unit = {},
)
