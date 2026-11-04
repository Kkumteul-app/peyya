package com.peyya.app.progress

import java.time.LocalDate

/**
 * 시동 리마인드 노출 정책. 순수 판정만 하며 I/O 없음.
 *
 * 시동 ON은 리마인드만 — 안내 시작이 아니다.
 * 「오늘 이용하지 않음」은 당일 프롬프트만 막고, 수동 startRoute는 ViewModel에서 계속 허용한다.
 */
sealed class IgnitionPromptResult {
    /** 리마인드 미노출 (스누즈·오늘안함·비Idle·시동 OFF 등). */
    data object ShowNothing : IgnitionPromptResult()

    /** 3 CTA: 운전 시작 / 나중에 확인 / 오늘 이용하지 않음. */
    data object ShowPrompt : IgnitionPromptResult()
}

data class PromptFlags(
    val phaseIdle: Boolean,
    val skipTodayDate: LocalDate?,
    val snoozeUntilEpochMs: Long?,
    val nowEpochMs: Long,
    val today: LocalDate,
)

object IgnitionPromptPolicy {

    /**
     * @param onEvent 시동 ON 이벤트(또는 현재 ON) 여부
     */
    fun evaluate(onEvent: Boolean, flags: PromptFlags): IgnitionPromptResult {
        if (!onEvent) return IgnitionPromptResult.ShowNothing
        if (!flags.phaseIdle) return IgnitionPromptResult.ShowNothing
        if (isSkippedToday(flags)) return IgnitionPromptResult.ShowNothing
        if (isSnoozeActive(flags)) return IgnitionPromptResult.ShowNothing
        return IgnitionPromptResult.ShowPrompt
    }

    /**
     * 스누즈 만료 epoch ms 반환.
     * [SnoozeChoice.AtReservation]은 [reservationAt]이 null이면 [IllegalArgumentException].
     */
    fun applySnooze(choice: SnoozeChoice, now: Long, reservationAt: Long?): Long =
        when (choice) {
            is SnoozeChoice.Minutes -> now + choice.minutes * 60_000L
            SnoozeChoice.AtReservation ->
                reservationAt
                    ?: throw IllegalArgumentException("AtReservation requires reservationAt")
        }

    private fun isSkippedToday(flags: PromptFlags): Boolean =
        flags.skipTodayDate != null && flags.skipTodayDate == flags.today

    private fun isSnoozeActive(flags: PromptFlags): Boolean {
        val until = flags.snoozeUntilEpochMs ?: return false
        return flags.nowEpochMs < until
    }
}
