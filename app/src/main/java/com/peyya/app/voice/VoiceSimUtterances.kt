package com.peyya.app.voice

/**
 * 시연용 음성 시뮬 칩이 [VoiceCommandSimulator]에 넘기는 발화.
 * UI 라벨과 파서 매칭이 어긋나지 않도록 단일 출처로 둔다.
 */
object VoiceSimUtterances {
    const val NEXT: String = "다음으로"
    const val SKIP: String = "건너뛰기"
    const val KEEP: String = "유지"

    const val LABEL_NEXT: String = "다음"
    const val LABEL_SKIP: String = "건너뛰기"
    const val LABEL_KEEP: String = "유지"
    const val SECTION_TITLE: String = "음성 시뮬 (시연)"
}
