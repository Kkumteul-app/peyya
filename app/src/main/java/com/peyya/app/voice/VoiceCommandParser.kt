package com.peyya.app.voice

/**
 * 짧은 음성 발화 → [VoiceCommand] 파서 (순수).
 * 실제 SpeechToText SDK 결과는 문자열로 들어온다고 가정한다.
 */
object VoiceCommandParser {
    fun parse(utterance: String): VoiceCommand {
        val normalized = utterance.trim().lowercase().replace("\\s+".toRegex(), "")
        if (normalized.isEmpty()) return VoiceCommand.Unknown

        return when {
            matchesNext(normalized) -> VoiceCommand.ConfirmNext
            matchesSkip(normalized) -> VoiceCommand.Skip
            matchesKeep(normalized) -> VoiceCommand.Keep
            else -> VoiceCommand.Unknown
        }
    }

    private fun matchesNext(text: String): Boolean =
        text.contains("다음으로") ||
            text == "다음" ||
            text.contains("안내해") ||
            text.contains("confirmnext") ||
            text == "next"

    private fun matchesSkip(text: String): Boolean =
        text.contains("건너뛰") ||
            text.contains("스킵") ||
            text == "skip"

    private fun matchesKeep(text: String): Boolean =
        text.contains("유지") ||
            text.contains("그대로") ||
            text == "keep"
}
