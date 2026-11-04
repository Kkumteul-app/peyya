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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * peyya2-wire: Completed 1회 → 리뷰 세션 1회.
 * peyya2-review-ui / course-ui: 별점 저장 · 시드 추천 · 코스 적용.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelReviewWireTest {

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
        loadSeed: Boolean = false,
    ): TripViewModel {
        if (loadSeed) reviewStore.loadSeedIfNeeded()
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
                idFactory = { "wired-session" },
            ),
            onTripCompleted = null,
            clock = clock,
            today = { today },
            pollIntervalMs = pollMs,
            tickIntervalMs = 1_000L,
            coroutineScope = scope,
        )
    }

    private suspend fun kotlinx.coroutines.test.TestScope.completeFullTrip(
        vm: TripViewModel,
        navi: MockNaviGateway,
        ignition: MockIgnitionGateway,
    ) {
        ignition.setOn()
        vm.startDriving()
        runCurrent()

        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        advanceTimeBy(dwellMs)
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.AwaitingNext)

        vm.confirmNext()
        runCurrent()
        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        advanceTimeBy(dwellMs)
        runCurrent()
    }

    @Test
    fun completed_startsReviewSession_once() = runTest {
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

        completeFullTrip(vm, navi, ignition)

        assertEquals(TripPhase.Completed, vm.uiState.value.phase)
        val review = vm.uiState.value.review
        assertNotNull(review)
        assertEquals("wired-session", review!!.sessionId)
        assertEquals(ReviewUiStep.Intro, review.step)
        assertEquals(1, reviewStore.loadSessions().count { it.status == TripReviewSessionStatus.InProgress })

        // 완료 콜백이 다시 불려도 세션 UI는 1개 유지
        vm.skipNext()
        runCurrent()
        assertEquals("wired-session", vm.uiState.value.review?.sessionId)
    }

    @Test
    fun reviewFlow_savesStopRatings_toReviewStore() = runTest {
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

        completeFullTrip(vm, navi, ignition)
        vm.beginReviewRating()
        vm.setReviewStars(5)
        vm.setReviewComment("최고")
        vm.commitReviewStop(carMode = false)
        assertEquals(1, vm.uiState.value.review?.stopIndex)

        vm.setReviewStars(4)
        vm.commitReviewStop(carMode = false)
        assertEquals(ReviewUiStep.Submitted, vm.uiState.value.review?.step)

        vm.acknowledgeReviewSubmitted()
        runCurrent()

        val completed = reviewStore.loadSessions().filter { it.status == TripReviewSessionStatus.Completed }
        assertEquals(1, completed.size)
        assertEquals(2, completed.single().stopReviews.size)
        assertEquals(5, completed.single().stopReviews[0].ratingStars)
        assertEquals(TripPhase.Idle, vm.uiState.value.phase)
        assertNull(vm.uiState.value.review)
    }

    @Test
    fun seed_exposesCourseOffer_andApplyUpdatesStops() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val reviewStore = InMemoryReviewStore()
        val vm = createVm(
            backgroundScope,
            navi,
            ignition,
            reviewStore,
            clock = { testScheduler.currentTime },
            loadSeed = true,
        )
        runCurrent()

        val offer = vm.uiState.value.courseOffer
        assertNotNull(offer)
        assertEquals(PresetTrips.weekendMuseumTrip.id, offer!!.courseId)

        vm.applyCourseOffer()
        runCurrent()

        assertEquals(PresetTrips.weekendMuseumTrip.title, vm.uiState.value.trip.title)
        assertTrue(vm.uiState.value.trip.stops.all { it.selected })
        assertTrue(vm.uiState.value.courseAppliedConfirm)
        assertNull(vm.uiState.value.courseOffer)

        vm.acknowledgeCourseApplied()
        assertEquals(TripPhase.Idle, vm.uiState.value.phase)
    }
}
