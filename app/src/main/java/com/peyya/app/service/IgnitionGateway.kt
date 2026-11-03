package com.peyya.app.service

interface IgnitionGateway {
    val isMock: Boolean

    fun initialize(): Result<Unit>

    /** UI / demo signal that ignition (시동) was pressed. */
    fun signalIgnition(): Result<Unit>

    fun release()
}
