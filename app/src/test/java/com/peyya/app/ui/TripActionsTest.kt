package com.peyya.app.ui

import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.viewmodel.TripUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripActionsTest {

    private val noop: () -> Unit = {}

    private fun state(
        phase: TripPhase,
        mockMode: Boolean = true,
    ) = TripUiState(
        family = PresetTrips.defaultFamily,
        trip = PresetTrips.weekendMuseumTrip,
        phase = phase,
        mockMode = mockMode,
    )

    private fun actionsFor(state: TripUiState) = resolveTripActions(
        state = state,
        onStartRoute = noop,
        onMarkArrived = noop,
        onIgnition = noop,
        onConfirmNext = noop,
        onSkipNext = noop,
        onResetTrip = noop,
    )

    @Test
    fun resolveTripActions_navigating_hasNoPrimaryArriveCta() {
        val actions = actionsFor(state(TripPhase.Navigating("museum")))
        assertNull(actions.primaryLabel)
        assertFalse(actions.primaryEnabled)
    }

    @Test
    fun resolveTripActions_idle_primaryIsStartDriving() {
        val actions = actionsFor(state(TripPhase.Idle))
        assertEquals("운전 시작", actions.primaryLabel)
        assertTrue(actions.primaryEnabled)
    }

    @Test
    fun resolveTripActions_arrived_mockAndReal_haveNoIgnitionPrimary() {
        val mock = actionsFor(state(TripPhase.Arrived("museum"), mockMode = true))
        val real = actionsFor(state(TripPhase.Arrived("museum"), mockMode = false))
        assertNull(mock.primaryLabel)
        assertNull(real.primaryLabel)
        assertNull(mock.secondaryLabel)
        assertNull(real.secondaryLabel)
    }

    @Test
    fun heroCopy_arrived_explainsOffOnOrHoldFiveMinutes() {
        val (_, subtitle) = heroCopy(state(TripPhase.Arrived("museum")))
        assertTrue(subtitle.contains("시동을 끄었다가 다시 켜거나"))
        assertTrue(subtitle.contains("5분"))
    }
}
