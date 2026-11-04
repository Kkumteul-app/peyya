package com.peyya.app.voice

/**
 * STT SDK 대체 시뮬레이터.
 * 시연·단위 테스트에서 발화 문자열을 주입한다.
 */
class VoiceCommandSimulator(
    private val parser: (String) -> VoiceCommand = VoiceCommandParser::parse,
) {
    private val history = mutableListOf<String>()

    fun uttered(): List<String> = history.toList()

    /** 시뮬 발화. 파싱된 명령을 반환한다. */
    fun simulate(utterance: String): VoiceCommand {
        history.add(utterance)
        return parser(utterance)
    }

    fun clear() {
        history.clear()
    }
}
