package com.peyya.app.voice

/**
 * 주행 중 짧은 STT 명령.
 * 복잡한 편집은 [DrivingEditLock]으로 잠근다.
 */
sealed interface VoiceCommand {
    /** 「다음」「다음으로」→ confirmNext */
    data object ConfirmNext : VoiceCommand

    /** 「건너뛰기」「스킵」 */
    data object Skip : VoiceCommand

    /** 「유지」「그대로」 */
    data object Keep : VoiceCommand

    data object Unknown : VoiceCommand
}
