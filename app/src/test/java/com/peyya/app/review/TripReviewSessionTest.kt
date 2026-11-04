package com.peyya.app.review

import com.peyya.app.progress.OnTripCompleted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripReviewSessionTest {

    private fun session(
        tripId: String = "trip-1",
        status: TripReviewSessionStatus = TripReviewSessionStatus.InProgress,
        stopReviews: List<StopReview> = emptyList(),
        completedAtEpochMs: Long? = null,
    ) = TripReviewSession(
        sessionId = "session-1",
        tripId = tripId,
        tripTitle = "남산 한바퀴",
        status = status,
        stopReviews = stopReviews,
        startedAtEpochMs = 1_700_000_000_000L,
        completedAtEpochMs = completedAtEpochMs,
    )

    @Test
    fun onTripCompleted_tripId_bindsToSession() {
        var started: TripReviewSession? = null
        val port = OnTripCompleted { tripId ->
            started = session(tripId = tripId)
        }

        port.onTripCompleted("completed-trip-42")

        assertEquals("completed-trip-42", started!!.tripId)
        assertEquals(TripReviewSessionStatus.InProgress, started!!.status)
    }

    @Test
    fun status_coversInProgressCompletedSkipped() {
        assertEquals(
            setOf("InProgress", "Completed", "Skipped"),
            TripReviewSessionStatus.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun inProgress_allowsNullCompletedAt() {
        val session = session(status = TripReviewSessionStatus.InProgress)
        assertNull(session.completedAtEpochMs)
    }

    @Test
    fun completed_keepsStopReviewsAndCompletedAt() {
        val stopReview = StopReview(
            stopId = "stop-1",
            poiId = "poi-1",
            stopName = "남산",
            ratingStars = 5,
            comment = "",
            sentiment = Sentiment(
                label = SentimentLabel.Positive,
                source = SentimentSource.Stars,
            ),
            reviewedAtEpochMs = 1_700_000_000_100L,
        )
        val session = session(
            status = TripReviewSessionStatus.Completed,
            stopReviews = listOf(stopReview),
            completedAtEpochMs = 1_700_000_000_200L,
        )
        assertEquals(1, session.stopReviews.size)
        assertEquals(1_700_000_000_200L, session.completedAtEpochMs)
    }
}
