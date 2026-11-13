package com.peyya.app.domain

enum class Sentiment { Positive, Neutral, Negative }

data class StopReview(
    val stopId: String,
    val stopName: String,
    val stars: Int,
    val comment: String = "",
    val sentiment: Sentiment,
) {
    init {
        require(stars in 1..5) { "stars 1-5" }
    }
}

data class TripReviewSession(
    val tripId: String,
    val reviews: List<StopReview>,
    val skipped: Boolean = false,
    val completedAtEpochMs: Long = System.currentTimeMillis(),
)

object SentimentLabeler {
    private val positiveWords = listOf("좋", "최고", "만족", "추천", "재밌", "편했", "맛있")
    private val negativeWords = listOf("별로", "실망", "불만", "복잡", "최악", "힘들", "대기")

    fun label(stars: Int, comment: String): Sentiment {
        val base = when (stars) {
            in 4..5 -> Sentiment.Positive
            3 -> Sentiment.Neutral
            else -> Sentiment.Negative
        }
        val text = comment.lowercase()
        val pos = positiveWords.any { text.contains(it) }
        val neg = negativeWords.any { text.contains(it) }
        return when {
            pos && !neg -> Sentiment.Positive
            neg && !pos -> Sentiment.Negative
            else -> base
        }
    }
}
