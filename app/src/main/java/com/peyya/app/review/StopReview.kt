package com.peyya.app.review

data class StopReview(
    val stopId: String,
    val poiId: String,
    val stopName: String,
    val ratingStars: Int,
    val comment: String,
    val sentiment: Sentiment,
    val reviewedAtEpochMs: Long,
) {
    init {
        require(ratingStars in 1..5) {
            "ratingStars must be in 1..5, was $ratingStars"
        }
    }
}
