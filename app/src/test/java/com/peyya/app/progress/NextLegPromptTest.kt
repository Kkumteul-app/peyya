package com.peyya.app.progress

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextLegPromptTest {

    private val dwellMs = 5 * 60 * 1000L

    private fun prompt(dwell: Long = dwellMs) = NextLegPrompt(dwellMs = dwell)

    @Test
    fun offThenOn_proposesOnce() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 1_000L)

        assertFalse(p.onIgnitionOn(1_100L))

        p.onIgnitionOff()
        assertTrue(p.onIgnitionOn(2_000L))
        assertFalse(p.onIgnitionOn(3_000L))
    }

    @Test
    fun ignitionHeld_proposesOnceAfterDwell() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 10_000L)

        assertFalse(p.onTick(now = 10_000L + dwellMs - 1, ignitionStillOn = true))
        assertTrue(p.onTick(now = 10_000L + dwellMs, ignitionStillOn = true))
        assertFalse(p.onTick(now = 10_000L + dwellMs + 1, ignitionStillOn = true))
    }

    @Test
    fun dwellPropose_thenOffOn_doesNotRepropose() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 0L)

        assertTrue(p.onTick(now = dwellMs, ignitionStillOn = true))

        p.onIgnitionOff()
        assertFalse(p.onIgnitionOn(dwellMs + 1_000L))
    }

    @Test
    fun offOnPropose_thenDwellTick_doesNotRepropose() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 0L)
        p.onIgnitionOff()
        assertTrue(p.onIgnitionOn(500L))

        assertFalse(p.onTick(now = dwellMs, ignitionStillOn = true))
    }

    @Test
    fun arrivedWithIgnitionOff_onProposes() {
        val p = prompt()
        p.onArrived(ignitionOn = false, atMs = 100L)

        assertTrue(p.onIgnitionOn(200L))
        assertFalse(p.onIgnitionOn(300L))
    }

    @Test
    fun tickWithIgnitionOff_doesNotProposeViaDwell() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 0L)

        assertFalse(p.onTick(now = dwellMs, ignitionStillOn = false))
    }

    @Test
    fun newArrivedSession_resetsProposeFlag() {
        val p = prompt()
        p.onArrived(ignitionOn = true, atMs = 0L)
        p.onIgnitionOff()
        assertTrue(p.onIgnitionOn(1L))

        p.onArrived(ignitionOn = true, atMs = 10_000L)
        p.onIgnitionOff()
        assertTrue(p.onIgnitionOn(10_100L))
    }
}
