package com.peyya.app.service

import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import android.util.Log
import com.peyya.app.domain.Stop

class PleosNaviGateway(
    context: Context,
) : NaviGateway {
    override val isMock: Boolean = false

    private val appContext = context.applicationContext
    private var helper: NaviHelper? = null
    private var ready = false

    override fun initialize(): Result<Unit> {
        return runCatching {
            val created = NaviHelper(appContext)
            created.initialize()
            helper = created
            ready = true
            Log.i(TAG, "Pleos NaviHelper initialized")
            Unit
        }.onFailure { error ->
            ready = false
            helper = null
            Log.e(TAG, "Pleos NaviHelper init failed", error)
        }
    }

    override fun requestRoute(stop: Stop): Result<Unit> {
        val navi = helper
        if (!ready || navi == null) {
            return Result.failure(IllegalStateException("NaviHelper not initialized"))
        }
        return runCatching {
            navi.requestRoute(stop.toRouteInfo())
            Log.i(TAG, "requestRoute ${stop.name}")
            Unit
        }.onFailure { error ->
            Log.e(TAG, "requestRoute failed for ${stop.name}", error)
        }
    }

    override fun release() {
        runCatching { helper?.release() }
            .onFailure { Log.e(TAG, "release failed", it) }
        helper = null
        ready = false
    }

    private fun Stop.toRouteInfo(): RouteInfo = RouteInfo(
        latitude = latitude,
        longitude = longitude,
        poiId = poiId,
        poiName = name,
        poiSubId = poiSubId,
        address = address,
    )

    companion object {
        private const val TAG = "PeyyaPleosNavi"
    }
}
