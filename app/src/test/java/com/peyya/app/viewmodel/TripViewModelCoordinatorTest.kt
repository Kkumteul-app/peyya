package com.peyya.app.viewmodel

import com.peyya.app.domain.TripPhase
import com.peyya.app.progress.ArrivalPath
import com.peyya.app.progress.InMemoryTripProgressStore
import com.peyya.app.progress.OnTripCompleted
import com.peyya.app.review.InMemoryReviewStore
import com.peyya.app.review.ReviewSessionStarter
import com.peyya.app.service.MockIgnitionGateway
import com.peyya.app.service.MockNaviGateway
import com.peyya.app.service.SpeechPort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * ViewModel이 Coordinator 이벤트를 구독하고,
 * 메뉴 수동 도착·운전 시작 API를 유지하는지 검증한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelCoordinatorTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)
    private val pollMs = 1_000L

    private fun speechNoOp(): SpeechPort = object : SpeechPort {
        override fun speak(message: String) = Unit
        override fun shutdown() = Unit
    }

    private fun createVm(
        scope: kotlinx.coroutines.CoroutineScope,
        navi: MockNaviGateway,
        ignition: MockIgnitionGateway,
        completed: MutableList<String>,
        clock: () -> Long,
        reviewStore: InMemoryReviewStore = InMemoryReviewStore(),
    ): TripViewModel = TripViewModel(
        navi = navi,
        ignition = ignition,
        speech = speechNoOp(),
        store = InMemoryTripProgressStore(),
        mockMode = true,
        reviewStore = reviewStore,
        reviewSessionStarter = ReviewSessionStarter(reviewStore, clock = clock),
        onTripCompleted = OnTripCompleted { id -> completed += id },
        clock = clock,
        today = { today },
        pollIntervalMs = pollMs,
        tickIntervalMs = 1_000L,
        coroutineScope = scope,
    )

    @Test
    fun startRoute_and_markArrivedFromMenu_updateUiStateViaCoordinator() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val completed = mutableListOf<String>()
        val vm = createVm(backgroundScope, navi, ignition, completed, clock = { testScheduler.currentTime })
        runCurrent()

        vm.startRoute()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Navigating)

        vm.markArrived()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Arrived)
        assertEquals(ArrivalPath.Menu, vm.uiState.value.lastArrivalPath)
    }

    @Test
    fun onIgnition_demo_setsIgnitionOn_andCanProposeNextAfterArrivedOffOn() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val completed = mutableListOf<String>()
        val vm = createVm(backgroundScope, navi, ignition, completed, clock = { testScheduler.currentTime })

        vm.startDriving()
        runCurrent()
        vm.markArrivedFromMenu()
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Arrived)

        ignition.setOff()
        runCurrent()
        vm.onIgnition()
        runCurrent()

        assertTrue(vm.uiState.value.phase is TripPhase.AwaitingNext)
    }

    @Test
    fun happyPath_poll_thenDwell_reachesAwaitingNext_throughViewModel() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val completed = mutableListOf<String>()
        val vm = createVm(backgroundScope, navi, ignition, completed, clock = { testScheduler.currentTime })

        ignition.setOn()
        vm.startDriving()
        runCurrent()

        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        assertTrue(vm.uiState.value.phase is TripPhase.Arrived)

        advanceTimeBy(5 * 60 * 1000L)
        runCurrent()
        val awaiting = vm.uiState.value.phase as TripPhase.AwaitingNext
        assertEquals("restaurant", awaiting.nextStopId)
    }
}
