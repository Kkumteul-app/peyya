package com.peyya.app.viewmodel

import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan

data class TripUiState(
    val family: FamilyProfile,
    val trip: TripPlan,
    val phase: TripPhase = TripPhase.Idle,
    val mockMode: Boolean = true,
    val statusMessage: String = "경유지를 확인한 뒤 오늘 루트를 시작하세요.",
    val lastSpoken: String = "",
)
