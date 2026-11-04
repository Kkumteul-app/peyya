package com.peyya.app.service

import android.util.Log
import com.peyya.app.domain.Stop
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MockNaviGateway : NaviGateway {
    override val isMock: Boolean = true

    private val arrivals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @Volatile
    private var remainingMeters: Double = DEFAULT_REMAINING_METERS

    @Volatile
    private var arrived: Boolean = false

    override fun initialize(): Result<Unit> {
        Log.i(TAG, "MockNavi initialize()")
        return Result.success(Unit)
    }

    override fun requestRoute(stop: Stop): Result<Unit> {
        arrived = false
        remainingMeters = DEFAULT_REMAINING_METERS
        Log.i(
            TAG,
            "MockNavi requestRoute name=${stop.name} lat=${stop.latitude} lng=${stop.longitude}",
        )
        return Result.success(Unit)
    }

    override fun pollGuidance(): Result<NaviGuidanceSnapshot> {
        return Result.success(
            NaviGuidanceSnapshot(
                remainingMeters = remainingMeters,
                isArrived = arrived,
                rawState = "mock remaining=$remainingMeters arrived=$arrived",
            ),
        )
    }

    override fun arrivalEvents(): Flow<Unit> = arrivals.asSharedFlow()

    /** 시뮬: 도착 이벤트 emit + 스냅샷 arrived */
    fun injectArrived() {
        arrived = true
        remainingMeters = 0.0
        val emitted = arrivals.tryEmit(Unit)
        Log.i(TAG, "MockNavi injectArrived emitted=$emitted")
    }

    /** 시뮬: 잔여거리(m) 주입. 0이면 폴링 도착 판정에 사용 가능 */
    fun setRemainingMeters(meters: Double) {
        remainingMeters = meters
        Log.i(TAG, "MockNavi setRemainingMeters meters=$meters")
    }

    override fun release() {
        Log.i(TAG, "MockNavi release()")
    }

    companion object {
        private const val TAG = "PeyyaMockNavi"
        private const val DEFAULT_REMAINING_METERS = 1_000.0
    }
}
