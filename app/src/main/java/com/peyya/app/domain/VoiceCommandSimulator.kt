package com.peyya.app.domain

enum class VoiceCommand {
    Next,
    Skip,
    Keep,
    Unknown,
}

object VoiceCommandSimulator {
    fun parse(raw: String): VoiceCommand {
        val t = raw.trim().lowercase()
        return when {
            t.contains("다음") || t.contains("확인") || t.contains("시작") || t == "next" -> VoiceCommand.Next
            t.contains("건너") || t.contains("스킵") || t == "skip" -> VoiceCommand.Skip
            t.contains("유지") || t.contains("기다려") || t == "keep" -> VoiceCommand.Keep
            else -> VoiceCommand.Unknown
        }
    }

    val demoChips: List<String> = listOf("다음으로", "건너뛰기", "유지")
}
