package com.peyya.app.voice

/**
 * 파싱된 음성 명령을 진행 액션 키로 매핑 (순수).
 * UI/ViewModel 배선은 후속 frontend task.
 */
enum class VoiceTripAction {
    ConfirmNext,
    SkipNext,
    Keep,
    None,
}

object VoiceActionBridge {
    fun toTripAction(command: VoiceCommand): VoiceTripAction = when (command) {
        VoiceCommand.ConfirmNext -> VoiceTripAction.ConfirmNext
        VoiceCommand.Skip -> VoiceTripAction.SkipNext
        VoiceCommand.Keep -> VoiceTripAction.Keep
        VoiceCommand.Unknown -> VoiceTripAction.None
    }
}
