package com.peyya.app.service

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import android.util.Log

/**
 * Best-effort Vehicle SDK wrapper. Ignition/power APIs vary by vehicle;
 * we initialize safely and treat [signalIgnition] as a logged demo cue.
 */
class VehicleIgnitionGateway(
    context: Context,
) : IgnitionGateway {
    override val isMock: Boolean = false

    private val vehicle = Vehicle(context.applicationContext)
    private var ready = false

    override fun initialize(): Result<Unit> {
        return runCatching {
            vehicle.initialize()
            ready = true
            Log.i(TAG, "Vehicle SDK initialized")
            Unit
        }.onFailure { error ->
            ready = false
            Log.e(TAG, "Vehicle SDK init failed", error)
        }
    }

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
