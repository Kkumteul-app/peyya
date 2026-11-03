package com.peyya.app.service

import android.content.Context
import android.util.Log

data class GatewayBundle(
    val navi: NaviGateway,
    val ignition: IgnitionGateway,
    val mockMode: Boolean,
)

object GatewayFactory {
    fun create(context: Context, forceMock: Boolean = false): GatewayBundle {
        if (forceMock) {
            Log.i(TAG, "Force mock mode")
            return mockBundle()
        }

        val pleosNavi = PleosNaviGateway(context)
        val naviResult = pleosNavi.initialize()
        if (naviResult.isFailure) {
            Log.w(TAG, "NaviHelper failed → MockNavi fallback")
            return mockBundle()
        }

        val vehicleIgnition = VehicleIgnitionGateway(context)
        val ignition = if (vehicleIgnition.initialize().isSuccess) {
            vehicleIgnition
        } else {
            Log.w(TAG, "Vehicle init failed → MockIgnition")
            MockIgnitionGateway().also { it.initialize() }
        }

        // Banner only when navigation itself is mocked; Vehicle mock alone stays silent.
        return GatewayBundle(navi = pleosNavi, ignition = ignition, mockMode = false)
    }

    private fun mockBundle(): GatewayBundle {
        val navi = MockNaviGateway().also { it.initialize() }
        val ignition = MockIgnitionGateway().also { it.initialize() }
        return GatewayBundle(navi = navi, ignition = ignition, mockMode = true)
    }

    private const val TAG = "PeyyaGatewayFactory"
}
