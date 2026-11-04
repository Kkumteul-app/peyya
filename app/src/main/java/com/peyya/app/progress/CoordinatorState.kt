package com.peyya.app.progress

import com.peyya.app.domain.TripPhase
import java.time.LocalDate

/**
 * Coordinator가 단일로 갱신하는 진행 상태. ViewModel이 [TripUiState]로 투영한다.
 */
data class CoordinatorState(
    val phase: TripPhase = TripPhase.Idle,
    val tripId: String? = null,
    val stopId: String? = null,
    val ignitionPromptVisible: Boolean = false,
    val arrivalConfirmVisible: Boolean = false,
    val snoozeUntilEpochMs: Long? = null,
    val skipTodayDate: LocalDate? = null,
    val reservationAtEpochMs: Long? = null,
    val nextLegProposed: Boolean = false,
    val lastArrivalPath: ArrivalPath? = null,
    val notice: String? = null,
    val lastSpoken: String? = null,
)
