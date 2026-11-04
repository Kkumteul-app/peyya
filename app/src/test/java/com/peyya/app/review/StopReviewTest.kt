package com.peyya.app.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopReviewTest {

    private fun sentiment() = Sentiment(
        label = SentimentLabel.Neutral,
        source = SentimentSource.Stars,
        matchedKeywords = emptyList(),
    )

    private fun review(
        ratingStars: Int = 3,
        comment: String = "",
    ) = StopReview(
        stopId = "stop-1",
        poiId = "poi-1",
        stopName = "한옥마을",
        ratingStars = ratingStars,
        comment = comment,
        sentiment = sentiment(),
        reviewedAtEpochMs = 1_700_000_000_000L,
    )

    @Test
    fun ratingStars_acceptsOneThroughFive() {
        (1..5).forEach { stars ->
            assertEquals(stars, review(ratingStars = stars).ratingStars)
        }
    }

    @Test
    fun ratingStars_rejectsBelowOne() {
        var thrown = false
        try {
            review(ratingStars = 0)
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun ratingStars_rejectsAboveFive() {
        var thrown = false
        try {
            review(ratingStars = 6)
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun comment_allowsEmptyString() {
        val review = review(comment = "")
        assertEquals("", review.comment)
    }

    @Test
    fun comment_keepsNonEmptyText() {
        val review = review(comment = "좋았어요")
        assertEquals("좋았어요", review.comment)
    }

    @Test
    fun fields_matchStopKeys() {
        val review = review()
        assertEquals("stop-1", review.stopId)
        assertEquals("poi-1", review.poiId)
        assertEquals("한옥마을", review.stopName)
    }
}
