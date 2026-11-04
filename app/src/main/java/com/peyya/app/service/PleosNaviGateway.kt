package com.peyya.app.service

import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.util.Log
import com.peyya.app.domain.Stop
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * PLEOS NaviHelper 연동.
 *
 * 관측 근거 (NaviHelper 3.0.5 / navi-data 3.0.4 jar):
 * - arrival: [NaviHelperEventListener.onDestinationArrived]
 * - remaining: [DestinationInfo.remainDistance] via [NaviHelper.getDestinationInfo]
 *   → 비동기 콜백 캐시. 동기 잔여거리 API는 없음.
 */
class PleosNaviGateway(
    context: Context,
) : NaviGateway {
    override val isMock: Boolean = false

    private val appContext = context.applicationContext
    private var helper: NaviHelper? = null
    private var ready = false

    private val arrivals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @Volatile
    private var latestSnapshot: NaviGuidanceSnapshot? = null

    private val eventListener = object : NaviHelperEventListener {
        override fun onDestinationArrived(info: DestinationArrivedInfo) {
            latestSnapshot = NaviGuidanceSnapshot(
                remainingMeters = 0.0,
                isArrived = true,
                rawState = "onDestinationArrived name=${info.name} distance=${info.distance}",
            )
            val emitted = arrivals.tryEmit(Unit)
            Log.i(TAG, "onDestinationArrived emitted=$emitted name=${info.name}")
        }

        override fun onDestinationInfo(info: DestinationInfo) {
            val prevArrived = latestSnapshot?.isArrived == true
            latestSnapshot = NaviGuidanceSnapshot(
                remainingMeters = info.remainDistance.toDouble(),
                isArrived = prevArrived,
                rawState = "onDestinationInfo remainDistance=${info.remainDistance} name=${info.name}",
            )
            Log.i(TAG, "onDestinationInfo remainDistance=${info.remainDistance}")
        }
    }

    override fun initialize(): Result<Unit> {
        return runCatching {
            val created = NaviHelper(appContext)
            created.initialize()
            created.addListener(eventListener)
            helper = created
            ready = true
            latestSnapshot = null
            Log.i(TAG, "Pleos NaviHelper initialized + listener")
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
            latestSnapshot = NaviGuidanceSnapshot(
                remainingMeters = Double.POSITIVE_INFINITY,
                isArrived = false,
                rawState = "requestRoute ${stop.name}",
            )
            navi.requestRoute(stop.toRouteInfo())
            Log.i(TAG, "requestRoute ${stop.name}")
            Unit
        }.onFailure { error ->
            Log.e(TAG, "requestRoute failed for ${stop.name}", error)
        }
    }

    override fun pollGuidance(): Result<NaviGuidanceSnapshot> {
        val navi = helper
        if (!ready || navi == null) {
            val error = IllegalStateException("NaviHelper not initialized")
            Log.e(TAG, "pollGuidance failed: not initialized", error)
            return Result.failure(error)
        }
        // 비동기 갱신 요청. 동기 remain API 없음 (jar: getDestinationInfo → onDestinationInfo).
        return runCatching {
            navi.getDestinationInfo()
            latestSnapshot
                ?: throw UnsupportedOperationException(
                    "Pleos pollGuidance: DestinationInfo not cached yet " +
                        "(async getDestinationInfo → onDestinationInfo)",
                )
        }.onFailure { error ->
            Log.e(TAG, "pollGuidance unsupported or unavailable", error)
        }
    }

    override fun arrivalEvents(): Flow<Unit> = arrivals.asSharedFlow()

    override fun release() {
        val navi = helper
        if (navi != null) {
            runCatching { navi.removeListener(eventListener) }
                .onFailure { Log.e(TAG, "removeListener failed", it) }
            runCatching { navi.release() }
                .onFailure { Log.e(TAG, "release failed", it) }
        }
        helper = null
        ready = false
        latestSnapshot = null
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
