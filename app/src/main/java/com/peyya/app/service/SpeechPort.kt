package com.peyya.app.service

/** TTS 경계. 단위 테스트에서 no-op 구현을 주입한다. */
interface SpeechPort {
    fun speak(message: String)

    fun shutdown()
}
