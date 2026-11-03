package com.peyya.app.service

import android.util.Log
import com.peyya.app.domain.Stop

class MockNaviGateway : NaviGateway {
    override val isMock: Boolean = true

    override fun initialize(): Result<Unit> {
        Log.i(TAG, "MockNavi initialize()")
        return Result.success(Unit)
    }

    override fun requestRoute(stop: Stop): Result<Unit> {
        Log.i(
            TAG,
            "MockNavi requestRoute name=${stop.name} lat=${stop.latitude} lng=${stop.longitude}",
        )
        return Result.success(Unit)
    }

    override fun release() {
        Log.i(TAG, "MockNavi release()")
    }

    companion object {
        private const val TAG = "PeyyaMockNavi"
    }
}
