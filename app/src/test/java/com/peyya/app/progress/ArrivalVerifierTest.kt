package com.peyya.app.progress

import com.peyya.app.service.NaviGuidanceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrivalVerifierTest {

    private var nowMs: Long = 0L

    private fun verifier(
        gpsDwellMs: Long = 5 * 60 * 1000L,
    ) = ArrivalVerifier(
        clock = { nowMs },
        gpsDwellMs = gpsDwellMs,
    )

    @Test
    fun event_path_confirmsWithEvent() {
        val decision = verifier().onNaviArrivalEvent()
        assertEquals(ArrivalDecision.Confirmed(ArrivalPath.Event), decision)
    }

    @Test
    fun poll_path_confirmsWhenIsArrived() {
        val snapshot = NaviGuidanceSnapshot(
            remainingMeters = 12.0,
            isArrived = true,
            rawState = "arrived",
        )
        val decision = verifier().onPoll(snapshot)
        assertEquals(ArrivalDecision.Confirmed(ArrivalPath.Poll), decision)
    }

    @Test
    fun poll_path_confirmsWhenRemainingMetersNonPositive() {
        val snapshot = NaviGuidanceSnapshot(
            remainingMeters = 0.0,
            isArrived = false,
            rawState = "remaining=0",
        )
        val decision = verifier().onPoll(snapshot)
        assertEquals(ArrivalDecision.Confirmed(ArrivalPath.Poll), decision)
    }

    @Test
    fun poll_path_watchingWhenStillEnRoute() {
        val snapshot = NaviGuidanceSnapshot(
            remainingMeters = 500.0,
            isArrived = false,
            rawState = "guiding",
        )
        val decision = verifier().onPoll(snapshot)
        assertEquals(ArrivalDecision.Watching, decision)
    }

    @Test
    fun user_gps_prompt_path_needsConfirmAfterDwell_thenConfirmsOnUser() {
        val v = verifier(gpsDwellMs = 5 * 60 * 1000L)
        nowMs = 10_000L
        assertEquals(
            ArrivalDecision.Watching,
            v.onGpsSample(atDestination = true, dwellStartedAt = 10_000L),
        )

        nowMs = 10_000L + 5 * 60 * 1000L
        assertEquals(
            ArrivalDecision.NeedsUserConfirm,
            v.onGpsSample(atDestination = true, dwellStartedAt = 10_000L),
        )

        assertEquals(
            ArrivalDecision.Confirmed(ArrivalPath.UserGpsPrompt),
            v.onUserConfirmFromPrompt(),
        )
    }

    @Test
    fun gps_dwell_doesNotAutoConfirmArrived() {
        val v = verifier(gpsDwellMs = 1_000L)
        nowMs = 5_000L
        val decision = v.onGpsSample(atDestination = true, dwellStartedAt = 0L)
        assertEquals(ArrivalDecision.NeedsUserConfirm, decision)
        assertFalse(decision is ArrivalDecision.Confirmed)
    }

    @Test
    fun gps_outsideOrNoDwell_staysWatching() {
        val v = verifier()
        nowMs = 1_000_000L
        assertEquals(
            ArrivalDecision.Watching,
            v.onGpsSample(atDestination = false, dwellStartedAt = 0L),
        )
        assertEquals(
            ArrivalDecision.Watching,
            v.onGpsSample(atDestination = true, dwellStartedAt = null),
        )
    }

    @Test
    fun menu_path_confirmsWithMenu() {
        val decision = verifier().onMenuConfirm()
        assertEquals(ArrivalDecision.Confirmed(ArrivalPath.Menu), decision)
    }

    @Test
    fun arrivalPath_coversFourRoutes() {
        val names = ArrivalPath.entries.map { it.name }.toSet()
        assertEquals(
            setOf("Event", "Poll", "UserGpsPrompt", "Menu"),
            names,
        )
    }

    @Test
    fun noApiConfirmsByElapsedSinceRouteStartOrInitialEta() {
        val methodNames = ArrivalVerifier::class.java.declaredMethods.map { it.name }.toSet()
        assertFalse(methodNames.contains("confirmByElapsedSinceRouteStart"))
        assertFalse(methodNames.contains("confirmByInitialEta"))
        assertFalse(methodNames.any { it.contains("Elapsed", ignoreCase = true) })
        assertFalse(methodNames.any { it.contains("InitialEta", ignoreCase = true) })
        assertTrue(methodNames.contains("onNaviArrivalEvent"))
        assertTrue(methodNames.contains("onPoll"))
        assertTrue(methodNames.contains("onGpsSample"))
        assertTrue(methodNames.contains("onMenuConfirm"))
        assertTrue(methodNames.contains("onUserConfirmFromPrompt"))
    }
}
