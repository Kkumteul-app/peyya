package com.peyya.app.progress

import com.peyya.app.service.NaviGuidanceSnapshot

/**
 * 도착 확정 체인. 순수 판정만 수행하며 I/O 없음.
 *
 * - 이벤트/폴링 arrived → [ArrivalDecision.Confirmed]
 * - GPS 목적지 체류 [gpsDwellMs] 이상 → [ArrivalDecision.NeedsUserConfirm] (자동 Arrived 금지)
 * - 출발 후 N분·최초 ETA만으로 확정하는 API는 제공하지 않는다
 */
sealed class ArrivalDecision {
    data class Confirmed(val path: ArrivalPath) : ArrivalDecision()
    data object NeedsUserConfirm : ArrivalDecision()
    data object Watching : ArrivalDecision()
}

class ArrivalVerifier(
    private val clock: () -> Long,
    private val geofenceRadiusM: Double = 80.0,
    private val gpsDwellMs: Long = 5 * 60 * 1000L,
) {
    /** 호출자가 geofence 판정에 쓸 반경(m). 판정 입력은 [onGpsSample]의 atDestination. */
    fun geofenceRadiusMeters(): Double = geofenceRadiusM

    fun onNaviArrivalEvent(): ArrivalDecision =
        ArrivalDecision.Confirmed(ArrivalPath.Event)

    fun onPoll(snapshot: NaviGuidanceSnapshot): ArrivalDecision =
        if (snapshot.isArrived || snapshot.remainingMeters <= 0.0) {
            ArrivalDecision.Confirmed(ArrivalPath.Poll)
        } else {
            ArrivalDecision.Watching
        }

    /**
     * @param atDestination 목적지 geofence 내부 여부 (호출자가 [geofenceRadiusMeters]로 판정)
     * @param dwellStartedAt 체류 시작 epoch ms. null이면 아직 체류 미시작 → Watching
     */
    fun onGpsSample(atDestination: Boolean, dwellStartedAt: Long?): ArrivalDecision {
        if (!atDestination || dwellStartedAt == null) {
            return ArrivalDecision.Watching
        }
        val elapsed = clock() - dwellStartedAt
        return if (elapsed >= gpsDwellMs) {
            ArrivalDecision.NeedsUserConfirm
        } else {
            ArrivalDecision.Watching
        }
    }

    fun onMenuConfirm(): ArrivalDecision =
        ArrivalDecision.Confirmed(ArrivalPath.Menu)

    fun onUserConfirmFromPrompt(): ArrivalDecision =
        ArrivalDecision.Confirmed(ArrivalPath.UserGpsPrompt)
}
