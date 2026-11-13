package com.peyya.app.domain

data class CourseCandidate(
    val id: String,
    val title: String,
    val stopIds: List<String>,
    val score: Double,
    val reason: String,
)

class ReviewStore {
    private val sessions = mutableListOf<TripReviewSession>()

    fun save(session: TripReviewSession) {
        sessions += session
    }

    fun completedTripCount(): Int = sessions.count { !it.skipped }

    fun reviewsForPoi(stopId: String): List<StopReview> =
        sessions.flatMap { it.reviews }.filter { it.stopId == stopId }

    fun allReviews(): List<StopReview> = sessions.flatMap { it.reviews }

    fun loadSeed() {
        if (sessions.isNotEmpty()) return
        repeat(3) { i ->
            save(
                TripReviewSession(
                    tripId = "seed-trip-$i",
                    reviews = listOf(
                        StopReview("museum", "국립중앙박물관", 5, "아이랑 좋았어요", Sentiment.Positive),
                        StopReview("restaurant", "용산 한식 담소 (데모)", 4, "맛있음", Sentiment.Positive),
                        StopReview("museum", "국립중앙박물관", 5, "추천", Sentiment.Positive),
                        StopReview("restaurant", "용산 한식 담소 (데모)", 3, "보통", Sentiment.Neutral),
                        StopReview("hangang-cafe", "한강 뷰 카페 라운지 (데모)", 4, "여유", Sentiment.Positive),
                    ),
                ),
            )
        }
    }
}

object CourseRecommender {
    const val M = 3
    const val N = 5

    fun recommend(store: ReviewStore, catalog: List<Stop>): List<CourseCandidate> {
        if (store.completedTripCount() < M) return emptyList()
        val eligible = catalog.filter { store.reviewsForPoi(it.id).size >= N }
        if (eligible.size < 2) return emptyList()
        val scored = eligible.map { stop ->
            val reviews = store.reviewsForPoi(stop.id)
            val score = reviews.sumOf {
                when (it.sentiment) {
                    Sentiment.Positive -> 2.0
                    Sentiment.Neutral -> 0.5
                    Sentiment.Negative -> -1.0
                } + it.stars * 0.2
            }
            stop to score
        }.sortedByDescending { it.second }
        val top = scored.take(3).map { it.first }
        return listOf(
            CourseCandidate(
                id = "course-${top.joinToString("-") { it.id }}",
                title = top.joinToString(" · ") { it.name.take(8) },
                stopIds = top.map { it.id },
                score = scored.take(3).sumOf { it.second },
                reason = "긍정 리뷰 가중 · M=$M N=$N",
            ),
        )
    }
}
