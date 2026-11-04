package com.peyya.app.viewmodel

import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.progress.InMemoryTripProgressStore
import com.peyya.app.review.InMemoryReviewStore
import com.peyya.app.review.ReviewSessionStarter
import com.peyya.app.review.ReviewUiStep
import com.peyya.app.review.TripReviewSessionStatus
import com.peyya.app.service.MockIgnitionGateway
import com.peyya.app.service.MockNaviGateway
import com.peyya.app.service.SpeechPort
import com.peyya.app.ui.adapt.AdaptUiVisibility
import com.peyya.app.ui.booking.BookingUiVisibility
import com.peyya.app.ui.review.showsReviewOrCourseUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * peyya-e2e-full: 계획→예매→출발→적응→완료→리뷰→추천 한 줄 통합.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PeyyaFullE2eFlowTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)
    private val pollMs = 1_000L
    private val dwellMs = 5 * 60 * 1000L

    private fun speechNoOp(): SpeechPort = object : SpeechPort {
        override fun speak(message: String) = Unit
        override fun shutdown() = Unit
    }

    private fun createVm(
        scope: kotlinx.coroutines.CoroutineScope,
        navi: MockNaviGateway,
        ignition: MockIgnitionGateway,
        reviewStore: InMemoryReviewStore,
        clock: () -> Long,
    ): TripViewModel {
        reviewStore.loadSeedIfNeeded()
        return TripViewModel(
            navi = navi,
            ignition = ignition,
            speech = speechNoOp(),
            store = InMemoryTripProgressStore(),
            mockMode = true,
            reviewStore = reviewStore,
            reviewSessionStarter = ReviewSessionStarter(
                store = reviewStore,
                clock = clock,
                idFactory = { "e2e-session" },
            ),
            onTripCompleted = null,
            clock = clock,
            today = { today },
            pollIntervalMs = pollMs,
            tickIntervalMs = 1_000L,
            coroutineScope = scope,
        )
    }

    @Test
    fun fullScenario_planBookStartAdaptCompleteReviewRecommend() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val reviewStore = InMemoryReviewStore()
        val vm = createVm(
            backgroundScope,
            navi,
            ignition,
            reviewStore,
            clock = { testScheduler.currentTime },
        )
        runCurrent()

        // 1) 계획 + 예매 (Idle 체크리스트·외부 URL)
        val idle = vm.uiState.value
        assertTrue(idle.phase is TripPhase.Idle)
        assertEquals(PresetTrips.weekendMuseumTrip.id, idle.trip.id)
        assertTrue(BookingUiVisibility.showsBookingPanel(idle.phase, idle.trip))
        val bookingStops = BookingUiVisibility.visibleBookingStops(idle.phase, idle.trip)
        assertEquals("museum", bookingStops.single().id)
        assertTrue(BookingUiVisibility.canOpenBookingUrl(bookingStops.single()))
        assertNotNull(idle.courseOffer)

        // 2) 출발
        ignition.setOn()
        vm.startDriving()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Navigating)
        assertFalse(
            BookingUiVisibility.showsBookingPanel(
                TripPhase.Navigating("restaurant"),
                vm.uiState.value.trip,
            ),
        )

        // 3) 적응 (박물관 도착 · 시연 우회 제안)
        vm.markArrivedFromMenu()
        runCurrent()
        val arrived = vm.uiState.value
        assertTrue(arrived.phase is TripPhase.Arrived)
        assertNotNull(arrived.adaptSuggestion)
        assertTrue(
            AdaptUiVisibility.showsAdaptPanel(arrived.phase, arrived.adaptSuggestion),
        )
        vm.dismissAdaptSuggestion()
        runCurrent()
        assertNull(vm.uiState.value.adaptSuggestion)

        // 4) 다음 다리 → 완료
        ignition.setOff()
        runCurrent()
        vm.onIgnition()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.AwaitingNext)
        vm.confirmNext()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Navigating)

        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        advanceTimeBy(dwellMs)
        runCurrent()

        val completed = vm.uiState.value
        assertEquals(TripPhase.Completed, completed.phase)
        assertNotNull(completed.review)
        assertEquals(ReviewUiStep.Intro, completed.review!!.step)
        assertTrue(completed.showsReviewOrCourseUi())

        // 5) 리뷰 (장소별 별점)
        vm.beginReviewRating()
        vm.setReviewStars(5)
        vm.setReviewComment("좋았어요")
        vm.commitReviewStop(carMode = false)
        assertEquals(1, vm.uiState.value.review?.stopIndex)
        vm.setReviewStars(4)
        vm.commitReviewStop(carMode = false)
        assertEquals(ReviewUiStep.Submitted, vm.uiState.value.review?.step)
        vm.acknowledgeReviewSubmitted()
        runCurrent()

        val saved = reviewStore.loadSessions()
            .filter { it.status == TripReviewSessionStatus.Completed && it.sessionId == "e2e-session" }
        assertEquals(1, saved.size)
        assertEquals(2, saved.single().stopReviews.size)

        // 6) 추천 (시드 코스 적용)
        val afterReview = vm.uiState.value
        assertTrue(afterReview.phase is TripPhase.Idle)
        assertNotNull(afterReview.courseOffer)
        assertEquals(PresetTrips.weekendMuseumTrip.id, afterReview.courseOffer!!.courseId)
        vm.applyCourseOffer()
        runCurrent()
        assertTrue(vm.uiState.value.courseAppliedConfirm)
        assertNull(vm.uiState.value.courseOffer)
        vm.acknowledgeCourseApplied()
        assertTrue(vm.uiState.value.phase is TripPhase.Idle)
    }
}
