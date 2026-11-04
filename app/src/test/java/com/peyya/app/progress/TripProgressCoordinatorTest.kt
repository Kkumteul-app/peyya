package com.peyya.app.progress

import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.service.MockIgnitionGateway
import com.peyya.app.service.MockNaviGateway
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TripProgressCoordinatorTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)
    private val pollMs = 1_000L
    private val tickMs = 1_000L
    private val dwellMs = 5 * 60 * 1000L

    private fun newCoordinator(
        scope: kotlinx.coroutines.CoroutineScope,
        navi: MockNaviGateway = MockNaviGateway().also { it.initialize() },
        ignition: MockIgnitionGateway = MockIgnitionGateway().also { it.initialize() },
        store: TripProgressStore = InMemoryTripProgressStore(),
        completed: MutableList<String> = mutableListOf(),
        clock: () -> Long,
    ): Triple<TripProgressCoordinator, MockNaviGateway, MockIgnitionGateway> {
        val coordinator = TripProgressCoordinator(
            navi = navi,
            ignition = ignition,
            store = store,
            scope = scope,
            clock = clock,
            today = { today },
            arrivalVerifier = ArrivalVerifier(clock = clock),
            nextLegPrompt = NextLegPrompt(dwellMs = dwellMs),
            onTripCompleted = OnTripCompleted { id -> completed += id },
            pollIntervalMs = pollMs,
            tickIntervalMs = tickMs,
        )
        coordinator.bindTrip(PresetTrips.weekendMuseumTrip)
        coordinator.start()
        return Triple(coordinator, navi, ignition)
    }

    @Test
    fun happyPath_navigate_pollArrive_ignitionHold5min_awaitingNext() = runTest {
        val completed = mutableListOf<String>()
        val (coordinator, navi, ignition) = newCoordinator(
            scope = backgroundScope,
            completed = completed,
            clock = { testScheduler.currentTime },
        )

        ignition.setOn()
        assertTrue(coordinator.startDriving())
        runCurrent()
        assertTrue(coordinator.state.value.phase is TripPhase.Navigating)

        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()

        val arrived = coordinator.state.value.phase as TripPhase.Arrived
        assertEquals("museum", arrived.stopId)
        assertEquals(ArrivalPath.Poll, coordinator.state.value.lastArrivalPath)

        advanceTimeBy(dwellMs)
        runCurrent()

        val awaiting = coordinator.state.value.phase as TripPhase.AwaitingNext
        assertEquals("restaurant", awaiting.nextStopId)
        assertTrue(coordinator.state.value.nextLegProposed)
        assertTrue(completed.isEmpty())
    }

    @Test
    fun gpsDwell_setsArrivalConfirmVisible_withoutAutoArrived() = runTest {
        val (coordinator, _, _) = newCoordinator(
            scope = backgroundScope,
            clock = { testScheduler.currentTime },
        )

        assertTrue(coordinator.startDriving())
        runCurrent()

        coordinator.onGpsSample(atDestination = true)
        assertFalse(coordinator.state.value.arrivalConfirmVisible)

        advanceTimeBy(dwellMs)
        runCurrent()
        coordinator.onGpsSample(atDestination = true)

        assertTrue(coordinator.state.value.arrivalConfirmVisible)
        assertTrue(coordinator.state.value.phase is TripPhase.Navigating)

        coordinator.confirmArrivalFromPrompt()
        assertTrue(coordinator.state.value.phase is TripPhase.Arrived)
        assertEquals(ArrivalPath.UserGpsPrompt, coordinator.state.value.lastArrivalPath)
        assertFalse(coordinator.state.value.arrivalConfirmVisible)
    }

    @Test
    fun completed_invokesOnTripCompleted_once() = runTest {
        val completed = mutableListOf<String>()
        val (coordinator, navi, ignition) = newCoordinator(
            scope = backgroundScope,
            completed = completed,
            clock = { testScheduler.currentTime },
        )

        ignition.setOn()
        assertTrue(coordinator.startDriving())
        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        advanceTimeBy(dwellMs)
        runCurrent()
        assertTrue(coordinator.state.value.phase is TripPhase.AwaitingNext)

        assertTrue(coordinator.confirmNext())
        navi.setRemainingMeters(0.0)
        advanceTimeBy(pollMs)
        runCurrent()
        advanceTimeBy(dwellMs)
        runCurrent()

        assertEquals(TripPhase.Completed, coordinator.state.value.phase)
        assertEquals(listOf("weekend-museum"), completed)

        coordinator.skipNext()
        assertEquals(1, completed.size)
    }

    @Test
    fun markArrivedFromMenu_confirmsWithMenuPath() = runTest {
        val (coordinator, _, _) = newCoordinator(
            scope = backgroundScope,
            clock = { testScheduler.currentTime },
        )
        assertTrue(coordinator.startDriving())
        coordinator.markArrivedFromMenu()
        assertTrue(coordinator.state.value.phase is TripPhase.Arrived)
        assertEquals(ArrivalPath.Menu, coordinator.state.value.lastArrivalPath)
    }

    @Test
    fun ignitionPrompt_showsOnIdleWhenOn_andHiddenAfterStartDriving() = runTest {
        val (coordinator, _, ignition) = newCoordinator(
            scope = backgroundScope,
            clock = { testScheduler.currentTime },
        )
        runCurrent()
        assertFalse(coordinator.state.value.ignitionPromptVisible)

        ignition.setOn()
        runCurrent()
        assertTrue(coordinator.state.value.ignitionPromptVisible)

        assertTrue(coordinator.startDriving())
        assertFalse(coordinator.state.value.ignitionPromptVisible)
    }

    @Test
    fun snooze_hidesPrompt_untilExpiry() = runTest {
        val (coordinator, _, ignition) = newCoordinator(
            scope = backgroundScope,
            clock = { testScheduler.currentTime },
        )
        ignition.setOn()
        runCurrent()
        assertTrue(coordinator.state.value.ignitionPromptVisible)

        coordinator.snooze(SnoozeChoice.Minutes(30))
        assertFalse(coordinator.state.value.ignitionPromptVisible)

        advanceTimeBy(30 * 60 * 1000L)
        runCurrent()
        assertTrue(coordinator.state.value.ignitionPromptVisible)
    }
}
