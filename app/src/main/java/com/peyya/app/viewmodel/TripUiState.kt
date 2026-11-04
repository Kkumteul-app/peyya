package com.peyya.app.viewmodel

import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.progress.ArrivalPath
import java.time.LocalDate

data class TripUiState(
    val family: FamilyProfile,
    val trip: TripPlan,
    val phase: TripPhase = TripPhase.Idle,
    val mockMode: Boolean = true,
    val notice: String? = null,
    val lastSpoken: String = "",
    val snackbar: String? = null,
    /** 시동 ON + Idle 조건 충족 시 리마인드 3선택 노출 */
    val ignitionPromptVisible: Boolean = false,
    /** GPS 체류 후 내비 미확정 — 사용자 「도착 완료」확인 */
    val arrivalConfirmVisible: Boolean = false,
    val snoozeUntilEpochMs: Long? = null,
    val skipTodayDate: LocalDate? = null,
    val lastArrivalPath: ArrivalPath? = null,
)
