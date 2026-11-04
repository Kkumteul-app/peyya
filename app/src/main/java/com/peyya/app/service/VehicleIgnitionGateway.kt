package com.peyya.app.service

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Best-effort Vehicle SDK wrapper.
 *
 * Vehicle 2.3.2 관측 근거:
 * - `PleosPropertyIds.IGNITION_STATE`, `IgnitionState` enum 은 vehicle-data에 존재
 * - 공개 facade (`Vehicle.get*` / `register*`) 에는 ignition get/observe API 없음
 * → [ignitionState] 는 Off sticky Flow + 로그 (signalIgnition 과 동일 패턴)
 */
class VehicleIgnitionGateway(
    context: Context,
) : IgnitionGateway {
    override val isMock: Boolean = false

    private val vehicle = Vehicle(context.applicationContext)
    private var ready = false

    private val _ignitionState = MutableStateFlow(IgnitionState.Off)

    override fun initialize(): Result<Unit> {
        return runCatching {
            vehicle.initialize()
            ready = true
            Log.i(TAG, "Vehicle SDK initialized")
            Log.i(
                TAG,
                "ignition observe unavailable: no public get/register ignition API " +
                    "(IGNITION_STATE constant only); Flow stays Off",
            )
            Unit
        }.onFailure { error ->
            ready = false
            Log.e(TAG, "Vehicle SDK init failed", error)
        }
    }

    override fun ignitionState(): Flow<IgnitionState> = _ignitionState.asStateFlow()

    override fun signalIgnition(): Result<Unit> {
        if (!ready) {
            return Result.failure(IllegalStateException("Vehicle not initialized"))
        }
        Log.i(TAG, "signalIgnition (UI simulator; no guaranteed power API)")
        return Result.success(Unit)
    }

    override fun release() {
        if (!ready) return
        runCatching { vehicle.release() }
            .onFailure { Log.e(TAG, "Vehicle release failed", it) }
        ready = false
    }

    companion object {
        private const val TAG = "PeyyaVehicleIgnition"
    }
}
