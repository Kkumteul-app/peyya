package com.peyya.app.ui.review

import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.review.ActiveReviewUi
import com.peyya.app.review.CourseCandidate
import com.peyya.app.review.ReviewUiStep
import com.peyya.app.viewmodel.TripUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUiVisibilityTest {

    private val trip = PresetTrips.weekendMuseumTrip

    private fun baseState() = TripUiState(
        family = PresetTrips.defaultFamily,
        trip = trip,
    )

    private fun sampleOffer() = CourseCandidate(
        courseId = trip.id,
        title = trip.title,
        stopPoiIds = trip.stops.map { it.poiId },
        stops = trip.stops,
        score = 1.0,
        positiveHitCount = 1,
        negativeHitCount = 0,
    )

    @Test
    fun idleWithoutOfferOrReview_hidesReviewCourseUi() {
        assertFalse(baseState().showsReviewOrCourseUi())
        assertFalse(baseState().showsInlineCourseCard())
    }

    @Test
    fun idleWithCourseOffer_keepsPlanHome_showsInlineCard() {
        val state = baseState().copy(phase = TripPhase.Idle, courseOffer = sampleOffer())
        assertFalse(state.showsReviewOrCourseUi())
        assertTrue(state.showsInlineCourseCard())
    }

    @Test
    fun navigatingWithCourseOffer_hidesInlineCard() {
        val state = baseState().copy(
            phase = TripPhase.Navigating("museum"),
            courseOffer = sampleOffer(),
        )
        assertFalse(state.showsReviewOrCourseUi())
        assertFalse(state.showsInlineCourseCard())
    }

    @Test
    fun activeReview_showsReviewCourseUi() {
        val state = baseState().copy(
            phase = TripPhase.Completed,
            review = ActiveReviewUi(
                sessionId = "s1",
                tripId = trip.id,
                tripTitle = trip.title,
                stops = trip.stops,
                step = ReviewUiStep.Intro,
            ),
            courseOffer = sampleOffer(),
        )
        assertTrue(state.showsReviewOrCourseUi())
        assertFalse(state.showsInlineCourseCard())
    }

    @Test
    fun courseAppliedConfirm_showsReviewCourseUi() {
        val state = baseState().copy(courseAppliedConfirm = true)
        assertTrue(state.showsReviewOrCourseUi())
        assertFalse(state.showsInlineCourseCard())
    }
}
