package com.peyya.app.service

import kotlinx.coroutines.flow.Flow

enum class IgnitionState {
    Off,
    On,
}

interface IgnitionGateway {
    val isMock: Boolean

    fun initialize(): Result<Unit>

    /** 시동 ON/OFF 관측. 초기값은 [IgnitionState.Off]. */
    fun ignitionState(): Flow<IgnitionState>

    /** UI / demo signal that ignition (시동) was pressed. */
    fun signalIgnition(): Result<Unit>

    fun release()
}
