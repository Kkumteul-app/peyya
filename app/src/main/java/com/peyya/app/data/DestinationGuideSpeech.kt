package com.peyya.app.data

import com.peyya.app.domain.Stop

/**
 * 차 내비 안내 중 TTS로 읽어 줄 목적지 브리핑.
 * 운전 중 듣기에 맞게 짧게 만든다.
 */
object DestinationGuideSpeech {
    fun forStop(stop: Stop): String {
        val tips = DestinationTipsCatalog.resolve(stop)
        val parts = mutableListOf<String>()
        parts += "${stop.name} 안내를 시작합니다. 가는 길에 참고하세요."

        tips.parkingTips.firstOrNull()?.let { parts += "주차 팁. $it" }
        tips.accessFacilities.take(3).takeIf { it.isNotEmpty() }?.let { facilities ->
            parts += "편의 시설은 ${facilities.joinToString(", ")}입니다."
        }
        tips.highlights.firstOrNull()?.let { parts += "볼거리. $it" }
        tips.familyTips.firstOrNull()?.let { parts += "가족 동반. $it" }

        return parts.joinToString(" ")
    }
}
