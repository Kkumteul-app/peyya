package com.peyya.app.service

import android.util.Log

class MockIgnitionGateway : IgnitionGateway {
    override val isMock: Boolean = true

    override fun initialize(): Result<Unit> {
        Log.i(TAG, "MockIgnition initialize()")
        return Result.success(Unit)
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
