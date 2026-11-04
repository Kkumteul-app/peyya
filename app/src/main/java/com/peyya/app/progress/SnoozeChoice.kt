package com.peyya.app.progress

/**
 * 「나중에 확인」스누즈 선택.
 * 고정 30/60/90분, 직접 입력(분), 예약 시각 알림.
 */
sealed class SnoozeChoice {
    /** 분 단위 스누즈. 30/60/90 또는 사용자가 입력한 custom 분. */
    data class Minutes(val minutes: Int) : SnoozeChoice()

    /** 예약 시각에 맞춰 다시 리마인드. [IgnitionPromptPolicy.applySnooze]에 reservationAt 필수. */
    data object AtReservation : SnoozeChoice()
}
