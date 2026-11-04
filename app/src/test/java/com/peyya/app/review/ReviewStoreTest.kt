package com.peyya.app.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewStoreTest {

    @Test
    fun save_then_loadSessions_returnsSaved() {
        val store = InMemoryReviewStore()
        val session = completedSession(
            sessionId = "s1",
            tripId = "trip-1",
            poiReviews = listOf(
                "peyya-demo-museum" to SentimentLabel.Positive,
                "peyya-demo-restaurant" to SentimentLabel.Neutral,
            ),
        )

        store.save(session)

        val loaded = store.loadSessions()
        assertEquals(1, loaded.size)
        assertEquals("s1", loaded.single().sessionId)
        assertEquals(TripReviewSessionStatus.Completed, loaded.single().status)
        assertEquals(2, loaded.single().stopReviews.size)
    }

    @Test
    fun restart_restore_inMemory_preservesSessions() {
        val first = InMemoryReviewStore()
        first.save(
            completedSession(
                sessionId = "s-restore",
                tripId = "trip-restore",
                poiReviews = listOf("poi-a" to SentimentLabel.Positive),
            ),
        )
        val exported = first.loadSessions()

        val restored = InMemoryReviewStore.fromSessions(exported)

        assertEquals(1, restored.loadSessions().size)
        assertEquals("s-restore", restored.loadSessions().single().sessionId)
        assertEquals(1, restored.completedSessionCount())
        assertEquals(1, restored.reviewCountForPoi("poi-a"))
    }

    @Test
    fun loadSeedIfNeeded_meetsCompletedRouteAndPoiReviewThresholds() {
        val store = InMemoryReviewStore()
        store.loadSeedIfNeeded()

        assertTrue(
            "seed completed routes must be ≥3, was ${store.completedSessionCount()}",
            store.completedSessionCount() >= 3,
        )
        val museumCount = store.reviewCountForPoi("peyya-demo-museum")
        val restaurantCount = store.reviewCountForPoi("peyya-demo-restaurant")
        assertTrue("museum reviews ≥5, was $museumCount", museumCount >= 5)
        assertTrue("restaurant reviews ≥5, was $restaurantCount", restaurantCount >= 5)
    }

    @Test
    fun loadSeedIfNeeded_isIdempotent() {
        val store = InMemoryReviewStore()
        store.loadSeedIfNeeded()
        val afterFirst = store.completedSessionCount()
        store.loadSeedIfNeeded()
        assertEquals(afterFirst, store.completedSessionCount())
    }

    @Test
    fun completedSessionCount_excludesSkippedAndInProgress() {
        val store = InMemoryReviewStore()
        store.save(
            completedSession(
                sessionId = "done",
                tripId = "t1",
                poiReviews = listOf("poi-x" to SentimentLabel.Positive),
            ),
        )
        store.save(
            TripReviewSession(
                sessionId = "skip",
                tripId = "t2",
                tripTitle = "skipped",
                status = TripReviewSessionStatus.Skipped,
                stopReviews = emptyList(),
                startedAtEpochMs = 1L,
                completedAtEpochMs = 2L,
            ),
        )
        store.save(
            TripReviewSession(
                sessionId = "wip",
                tripId = "t3",
                tripTitle = "wip",
                status = TripReviewSessionStatus.InProgress,
                stopReviews = emptyList(),
                startedAtEpochMs = 1L,
                completedAtEpochMs = null,
            ),
        )

        assertEquals(1, store.completedSessionCount())
    }

    @Test
    fun store_and_seed_doNotPersistGpsCoordinates() {
        val store = InMemoryReviewStore()
        store.loadSeedIfNeeded()

        val stopReviewFields = StopReview::class.java.declaredFields.map { it.name }.toSet()
        assertFalse(stopReviewFields.contains("latitude"))
        assertFalse(stopReviewFields.contains("longitude"))
        assertFalse(stopReviewFields.contains("lat"))
        assertFalse(stopReviewFields.contains("lng"))

        store.loadSessions().forEach { session ->
            session.stopReviews.forEach { review ->
                assertTrue(review.poiId.isNotBlank())
                assertTrue(review.stopName.isNotBlank())
            }
        }
    }

    private fun completedSession(
        sessionId: String,
        tripId: String,
        poiReviews: List<Pair<String, SentimentLabel>>,
    ): TripReviewSession {
        val reviews = poiReviews.mapIndexed { index, (poiId, label) ->
            StopReview(
                stopId = "stop-$index",
                poiId = poiId,
                stopName = "stop-$poiId",
                ratingStars = when (label) {
                    SentimentLabel.Positive -> 5
                    SentimentLabel.Neutral -> 3
                    SentimentLabel.Negative -> 1
                },
                comment = "",
                sentiment = Sentiment(label = label, source = SentimentSource.Stars),
                reviewedAtEpochMs = 1_700_000_000_000L + index,
            )
        }
        return TripReviewSession(
            sessionId = sessionId,
            tripId = tripId,
            tripTitle = "title-$tripId",
            status = TripReviewSessionStatus.Completed,
            stopReviews = reviews,
            startedAtEpochMs = 1_700_000_000_000L,
            completedAtEpochMs = 1_700_000_000_500L,
        )
    }
}
