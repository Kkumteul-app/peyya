package com.peyya.app.service

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockIgnitionGateway : IgnitionGateway {
    override val isMock: Boolean = true

    private val _ignitionState = MutableStateFlow(IgnitionState.Off)

    override fun initialize(): Result<Unit> {
        Log.i(TAG, "MockIgnition initialize()")
        return Result.success(Unit)
    }

    override fun ignitionState(): Flow<IgnitionState> = _ignitionState.asStateFlow()

    /** 시뮬: 시동 ON */
    fun setOn() {
        _ignitionState.value = IgnitionState.On
        Log.i(TAG, "MockIgnition setOn()")
    }

    /** 시뮬: 시동 OFF */
    fun setOff() {
        _ignitionState.value = IgnitionState.Off
        Log.i(TAG, "MockIgnition setOff()")
    }

    override fun signalIgnition(): Result<Unit> {
        Log.i(TAG, "MockIgnition signalIgnition()")
        return Result.success(Unit)
    }

    override fun release() {
        Log.i(TAG, "MockIgnition release()")
    }

    companion object {
        private const val TAG = "PeyyaMockIgnition"
    }
}
