package com.peyya.app.voice

import com.peyya.app.domain.TripPhase

/**
 * 주행 중 복잡한 편집 잠금.
 * Idle(정차·사전 계획)에서만 스톱 선택 등 편집을 허용한다.
 */
object DrivingEditLock {
    fun isEditingAllowed(phase: TripPhase): Boolean = phase is TripPhase.Idle

    fun isVoiceCommandAllowed(phase: TripPhase): Boolean =
        phase is TripPhase.AwaitingNext ||
            phase is TripPhase.Arrived ||
            phase is TripPhase.Navigating
}
