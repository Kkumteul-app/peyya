package com.peyya.app.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IgnitionPromptPolicyTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)
    private val nowMs: Long = 1_000_000L

    private fun flags(
        phaseIdle: Boolean = true,
        skipTodayDate: LocalDate? = null,
        snoozeUntilEpochMs: Long? = null,
        nowEpochMs: Long = nowMs,
        todayDate: LocalDate = today,
    ) = PromptFlags(
        phaseIdle = phaseIdle,
        skipTodayDate = skipTodayDate,
        snoozeUntilEpochMs = snoozeUntilEpochMs,
        nowEpochMs = nowEpochMs,
        today = todayDate,
    )

    @Test
    fun ignitionOn_idle_snoozeExpired_notSkipped_showsPrompt() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(snoozeUntilEpochMs = nowMs - 1L),
        )
        assertEquals(IgnitionPromptResult.ShowPrompt, result)
    }

    @Test
    fun ignitionOn_idle_noSnooze_showsPrompt() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(snoozeUntilEpochMs = null),
        )
        assertEquals(IgnitionPromptResult.ShowPrompt, result)
    }

    @Test
    fun snoozeActive_showsNothing() {
        val until = nowMs + 30 * 60_000L
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(snoozeUntilEpochMs = until),
        )
        assertEquals(IgnitionPromptResult.ShowNothing, result)
    }

    @Test
    fun snoozeExpired_thenIgnition_showsPrompt() {
        val until = nowMs + 30 * 60_000L
        assertEquals(
            IgnitionPromptResult.ShowNothing,
            IgnitionPromptPolicy.evaluate(
                onEvent = true,
                flags = flags(snoozeUntilEpochMs = until, nowEpochMs = nowMs),
            ),
        )
        assertEquals(
            IgnitionPromptResult.ShowPrompt,
            IgnitionPromptPolicy.evaluate(
                onEvent = true,
                flags = flags(snoozeUntilEpochMs = until, nowEpochMs = until),
            ),
        )
    }

    @Test
    fun skipToday_showsNothing() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(skipTodayDate = today),
        )
        assertEquals(IgnitionPromptResult.ShowNothing, result)
    }

    @Test
    fun skipOtherDay_stillShowsPrompt() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(skipTodayDate = today.minusDays(1)),
        )
        assertEquals(IgnitionPromptResult.ShowPrompt, result)
    }

    @Test
    fun notIdle_showsNothing() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = true,
            flags = flags(phaseIdle = false),
        )
        assertEquals(IgnitionPromptResult.ShowNothing, result)
    }

    @Test
    fun ignitionOff_showsNothing() {
        val result = IgnitionPromptPolicy.evaluate(
            onEvent = false,
            flags = flags(),
        )
        assertEquals(IgnitionPromptResult.ShowNothing, result)
    }

    @Test
    fun applySnooze_minutes30_60_90() {
        assertEquals(nowMs + 30 * 60_000L, IgnitionPromptPolicy.applySnooze(SnoozeChoice.Minutes(30), nowMs, null))
        assertEquals(nowMs + 60 * 60_000L, IgnitionPromptPolicy.applySnooze(SnoozeChoice.Minutes(60), nowMs, null))
        assertEquals(nowMs + 90 * 60_000L, IgnitionPromptPolicy.applySnooze(SnoozeChoice.Minutes(90), nowMs, null))
    }

    @Test
    fun applySnooze_customMinutes() {
        assertEquals(
            nowMs + 45 * 60_000L,
            IgnitionPromptPolicy.applySnooze(SnoozeChoice.Minutes(45), nowMs, null),
        )
    }

    @Test
    fun applySnooze_atReservation() {
        val reservationAt = nowMs + 2 * 60 * 60_000L
        assertEquals(
            reservationAt,
            IgnitionPromptPolicy.applySnooze(SnoozeChoice.AtReservation, nowMs, reservationAt),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun applySnooze_atReservationWithoutTime_throws() {
        IgnitionPromptPolicy.applySnooze(SnoozeChoice.AtReservation, nowMs, null)
    }

    @Test
    fun snoozeChoice_coversFixedCustomAndReservation() {
        val samples: List<SnoozeChoice> = listOf(
            SnoozeChoice.Minutes(30),
            SnoozeChoice.Minutes(60),
            SnoozeChoice.Minutes(90),
            SnoozeChoice.Minutes(15),
            SnoozeChoice.AtReservation,
        )
        assertEquals(5, samples.size)
        assertTrue(samples.any { it is SnoozeChoice.Minutes && it.minutes == 30 })
        assertTrue(samples.contains(SnoozeChoice.AtReservation))
    }
}
