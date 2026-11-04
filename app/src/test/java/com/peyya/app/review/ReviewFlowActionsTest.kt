package com.peyya.app.review

import com.peyya.app.data.PresetTrips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewFlowActionsTest {

    private val stops = PresetTrips.weekendMuseumTrip.selectedStops()

    private fun intro(): ActiveReviewUi = ActiveReviewUi(
        sessionId = "s1",
        tripId = "weekend-museum",
        tripTitle = PresetTrips.weekendMuseumTrip.title,
        stops = stops,
    )

    @Test
    fun commit_requiresStars() {
        val rating = ReviewFlowActions.beginRating(intro())
        val result = ReviewFlowActions.commitCurrentStop(rating, nowEpochMs = 10L)
        assertEquals(ReviewFlowActions.CommitResult.NeedsStars, result)
    }

    @Test
    fun commit_advancesThenFinishes_withSentiment() {
        var ui = ReviewFlowActions.beginRating(intro())
        ui = ReviewFlowActions.setStars(ui, 5)
        ui = ReviewFlowActions.setComment(ui, "최고였어요")

        val first = ReviewFlowActions.commitCurrentStop(ui, nowEpochMs = 11L)
        assertTrue(first is ReviewFlowActions.CommitResult.Advanced)
        ui = (first as ReviewFlowActions.CommitResult.Advanced).ui
        assertEquals(1, ui.stopIndex)
        assertEquals(1, ui.collected.size)
        assertEquals(SentimentLabel.Positive, ui.collected[0].sentiment.label)

        ui = ReviewFlowActions.setStars(ui, 2)
        val second = ReviewFlowActions.commitCurrentStop(ui, nowEpochMs = 12L)
        assertTrue(second is ReviewFlowActions.CommitResult.Finished)
        val finished = second as ReviewFlowActions.CommitResult.Finished
        assertEquals(2, finished.collected.size)
        assertEquals(ReviewUiStep.Submitted, finished.nextUi?.step)
    }

    @Test
    fun carMode_finish_clearsNextUi() {
        var ui = ReviewFlowActions.beginRating(intro())
        ui = ReviewFlowActions.setStars(ui, 4)
        val first = ReviewFlowActions.commitCurrentStop(ui, 1L) as ReviewFlowActions.CommitResult.Advanced
        ui = ReviewFlowActions.setStars(first.ui, 3)
        val done = ReviewFlowActions.commitCurrentStop(
            ui,
            nowEpochMs = 2L,
            carModeSkipsSubmitted = true,
        ) as ReviewFlowActions.CommitResult.Finished
        assertNull(done.nextUi)
        assertEquals(2, done.collected.size)
    }

    @Test
    fun skipCurrent_doesNotCollectReview() {
        var ui = ReviewFlowActions.beginRating(intro())
        val skipped = ReviewFlowActions.skipCurrentStop(ui) as ReviewFlowActions.CommitResult.Advanced
        assertTrue(skipped.ui.collected.isEmpty())
        assertEquals(1, skipped.ui.stopIndex)
    }
}
