package com.peyya.app.ui.voice

import com.peyya.app.domain.TripPhase
import com.peyya.app.voice.DrivingEditLock

/** 음성 시뮬 칩·주행 편집 잠금 힌트 표시. */
object VoiceUiVisibility {
    const val EDIT_LOCK_HINT: String = "주행 중에는 경유지를 바꿀 수 없어요"

    fun showsVoiceSimChips(phase: TripPhase, mockMode: Boolean): Boolean =
        mockMode && DrivingEditLock.isVoiceCommandAllowed(phase)

    fun showsEditLockHint(phase: TripPhase): Boolean =
        !DrivingEditLock.isEditingAllowed(phase)

    fun isEditingAllowed(phase: TripPhase): Boolean =
        DrivingEditLock.isEditingAllowed(phase)
}
