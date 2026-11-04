package com.peyya.app.viewmodel

import com.peyya.app.adapt.AdaptMockCatalog
import com.peyya.app.domain.TripPhase
import com.peyya.app.progress.InMemoryTripProgressStore
import com.peyya.app.review.InMemoryReviewStore
import com.peyya.app.review.ReviewSessionStarter
import com.peyya.app.service.MockIgnitionGateway
import com.peyya.app.service.MockNaviGateway
import com.peyya.app.service.SpeechPort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelAdaptVoiceTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)

    private fun speechNoOp(): SpeechPort = object : SpeechPort {
        override fun speak(message: String) = Unit
        override fun shutdown() = Unit
    }

    private fun createVm(
        scope: kotlinx.coroutines.CoroutineScope,
        navi: MockNaviGateway,
        ignition: MockIgnitionGateway,
        clock: () -> Long,
    ): TripViewModel {
        val reviewStore = InMemoryReviewStore()
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
            ),
            clock = clock,
            today = { today },
            pollIntervalMs = 1_000L,
            tickIntervalMs = 1_000L,
            coroutineScope = scope,
        )
    }

    @Test
    fun arrivedMuseum_exposesAdaptSuggestion_acceptRequestsRoute() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val vm = createVm(backgroundScope, navi, ignition, clock = { testScheduler.currentTime })
        runCurrent()

        ignition.setOn()
        vm.startDriving()
        runCurrent()
        vm.markArrivedFromMenu()
        runCurrent()

        assertTrue(vm.uiState.value.phase is TripPhase.Arrived)
        val suggestion = vm.uiState.value.adaptSuggestion
        assertNotNull(suggestion)
        assertTrue(suggestion!!.isDemoEstimate)
        assertEquals(AdaptMockCatalog.DEMO_LABEL, "시연용/추정")
        assertTrue(suggestion.detours.isNotEmpty())

        val detourId = suggestion.detours.first().id
        vm.acceptAdaptCandidate(detourId)
        runCurrent()

        assertNull(vm.uiState.value.adaptSuggestion)
        assertTrue(vm.uiState.value.snackbar?.contains("안내") == true)
    }

    @Test
    fun voiceSim_confirmNext_fromAwaitingNext() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val vm = createVm(backgroundScope, navi, ignition, clock = { testScheduler.currentTime })
        runCurrent()

        vm.startDriving()
        runCurrent()
        vm.markArrivedFromMenu()
        runCurrent()

        ignition.setOff()
        runCurrent()
        vm.onIgnition()
        runCurrent()

        assertTrue(vm.uiState.value.phase is TripPhase.AwaitingNext)
        vm.simulateVoiceUtterance("다음으로")
        runCurrent()

        assertTrue(vm.uiState.value.phase is TripPhase.Navigating)
    }

    @Test
    fun dismissAdapt_hidesPanel() = runTest {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        val vm = createVm(backgroundScope, navi, ignition, clock = { testScheduler.currentTime })
        runCurrent()

        ignition.setOn()
        vm.startDriving()
        runCurrent()
        vm.markArrivedFromMenu()
        runCurrent()
        assertNotNull(vm.uiState.value.adaptSuggestion)

        vm.dismissAdaptSuggestion()
        runCurrent()
        assertNull(vm.uiState.value.adaptSuggestion)
    }
}
