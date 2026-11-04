package com.peyya.app.review

import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseRecommenderTest {

    private val museum = Stop(
        id = "museum",
        name = "국립중앙박물관",
        address = "addr",
        latitude = 37.5,
        longitude = 126.9,
        poiId = "peyya-demo-museum",
    )
    private val restaurant = Stop(
        id = "restaurant",
        name = "용산 한식 담소",
        address = "addr",
        latitude = 37.5,
        longitude = 126.9,
        poiId = "peyya-demo-restaurant",
    )
    private val catalog = listOf(
        TripPlan(
            id = "weekend-museum",
            title = "주말 박물관 · 맛집 루트",
            stops = listOf(museum, restaurant),
        ),
    )

    @Test
    fun recommend_whenCompletedBelowM_returnsEmpty() {
        val store = InMemoryReviewStore()
        repeat(2) { i ->
            store.save(sessionWithPois(i, listOf("peyya-demo-museum", "peyya-demo-restaurant")))
        }

        val result = CourseRecommender.recommend(store, catalog)

        assertTrue(result.isEmpty())
        assertTrue(store.completedSessionCount() < CourseRecommender.M)
    }

    @Test
    fun recommend_excludesPoisBelowN_fromScore() {
        val store = InMemoryReviewStore()
        // 5 reviews for museum (meets N), only 2 for restaurant (below N)
        repeat(5) { i ->
            store.save(
                sessionWithPois(
                    index = i,
                    poiIds = if (i < 2) {
                        listOf("peyya-demo-museum", "peyya-demo-restaurant")
                    } else {
                        listOf("peyya-demo-museum")
                    },
                    label = SentimentLabel.Positive,
                ),
            )
        }

        val result = CourseRecommender.recommend(store, catalog)

        assertEquals(1, result.size)
        val candidate = result.single()
        assertEquals(5, candidate.positiveHitCount)
        assertEquals(0, candidate.negativeHitCount)
        assertEquals(
            5 * CourseRecommender.POSITIVE_WEIGHT,
            candidate.score,
            0.0001,
        )
    }

    @Test
    fun recommend_withSeed_returnsAtLeastOneCandidate_usingPositiveWeightAndNegativePenalty() {
        val store = InMemoryReviewStore()
        store.loadSeedIfNeeded()

        val result = CourseRecommender.recommend(store, catalog)

        assertTrue(result.isNotEmpty())
        val top = result.first()
        assertTrue(top.score > 0.0 || top.positiveHitCount > 0 || top.negativeHitCount > 0)
        assertEquals("weekend-museum", top.courseId)
        assertEquals(2, top.stopPoiIds.size)
        assertEquals(listOf(museum, restaurant).map { it.poiId }, top.stopPoiIds)
    }

    @Test
    fun recommend_appliesNegativePenalty() {
        val store = InMemoryReviewStore()
        repeat(5) { i ->
            store.save(
                sessionWithPois(
                    index = i,
                    poiIds = listOf("peyya-demo-museum"),
                    label = if (i < 3) SentimentLabel.Positive else SentimentLabel.Negative,
                ),
            )
        }

        val singlePoiCatalog = listOf(
            TripPlan(id = "museum-only", title = "박물관만", stops = listOf(museum)),
        )
        val result = CourseRecommender.recommend(store, singlePoiCatalog)

        assertEquals(1, result.size)
        val expected =
            3 * CourseRecommender.POSITIVE_WEIGHT +
                2 * CourseRecommender.NEGATIVE_PENALTY
        assertEquals(expected, result.single().score, 0.0001)
        assertEquals(3, result.single().positiveHitCount)
        assertEquals(2, result.single().negativeHitCount)
    }

    @Test
    fun recommend_whenAllCoursePoisBelowN_skipsCourse() {
        val store = InMemoryReviewStore()
        repeat(3) { i ->
            store.save(
                sessionWithPois(
                    index = i,
                    poiIds = listOf("peyya-demo-museum"),
                    label = SentimentLabel.Positive,
                ),
            )
        }
        // M=3 met, but museum has only 3 reviews (<N=5)
        assertEquals(3, store.completedSessionCount())
        assertTrue(store.reviewCountForPoi("peyya-demo-museum") < CourseRecommender.N)

        val result = CourseRecommender.recommend(store, catalog)
        assertTrue(result.isEmpty())
    }

    private fun sessionWithPois(
        index: Int,
        poiIds: List<String>,
        label: SentimentLabel = SentimentLabel.Positive,
    ): TripReviewSession {
        val stars = when (label) {
            SentimentLabel.Positive -> 5
            SentimentLabel.Neutral -> 3
            SentimentLabel.Negative -> 1
        }
        val reviews = poiIds.mapIndexed { j, poiId ->
            StopReview(
                stopId = "stop-$index-$j",
                poiId = poiId,
                stopName = poiId,
                ratingStars = stars,
                comment = "",
                sentiment = Sentiment(label = label, source = SentimentSource.Stars),
                reviewedAtEpochMs = 1_700_000_000_000L + index * 10 + j,
            )
        }
        return TripReviewSession(
            sessionId = "session-$index",
            tripId = "trip-$index",
            tripTitle = "trip-$index",
            status = TripReviewSessionStatus.Completed,
            stopReviews = reviews,
            startedAtEpochMs = 1_700_000_000_000L,
            completedAtEpochMs = 1_700_000_000_100L + index,
        )
    }
}
