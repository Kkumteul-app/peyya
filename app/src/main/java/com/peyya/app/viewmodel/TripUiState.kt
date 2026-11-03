package com.peyya.app.viewmodel

import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan

data class TripUiState(
    val family: FamilyProfile,
    val trip: TripPlan,
    val phase: TripPhase = TripPhase.Idle,
    val mockMode: Boolean = true,
    val notice: String? = null,
    val lastSpoken: String = "",
    val snackbar: String? = null,
)
