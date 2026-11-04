package com.peyya.app.progress

import com.peyya.app.domain.TripPhase
import java.time.LocalDate

/**
 * 여정 진행 로컬 영속 DTO.
 *
 * GPS 상세 좌표(lat/lng)는 포함하지 않는다 — 디버그 플래그 없이 저장 금지.
 */
data class TripProgressSnapshot(
    val tripId: String? = null,
    val phase: TripPhase = TripPhase.Idle,
    /** 현재 관련 stop (Navigating/Arrived stopId, AwaitingNext nextStopId). */
    val stopId: String? = null,
    val snoozeUntilEpochMs: Long? = null,
    val skipTodayDate: LocalDate? = null,
    val reservationAtEpochMs: Long? = null,
    val nextLegProposed: Boolean = false,
    val lastArrivalPath: ArrivalPath? = null,
)

/**
 * [skipTodayDate]가 [today]와 다르면 당일 스킵을 해제한다 (날짜 경과 리셋).
 */
fun TripProgressSnapshot.clearedSkipIfStale(today: LocalDate): TripProgressSnapshot {
    val skip = skipTodayDate ?: return this
    return if (skip != today) copy(skipTodayDate = null) else this
}
