package com.peyya.app.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.peyya.app.viewmodel.TripUiState

@Composable
fun TripScreen(
    state: TripUiState,
    onFamilyNameChange: (String) -> Unit,
    onToggleStop: (String, Boolean) -> Unit,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
) {
    val landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    // Keep signature for family name edits on phone; car uses glance-only names.
    @Suppress("UNUSED_PARAMETER")
    val unusedFamilyEditor = onFamilyNameChange

    if (landscape) {
        CarTripScreen(
            state = state,
            onToggleStop = onToggleStop,
            onStartRoute = onStartRoute,
            onMarkArrived = onMarkArrived,
            onIgnition = onIgnition,
            onConfirmNext = onConfirmNext,
            onSkipNext = onSkipNext,
            onResetTrip = onResetTrip,
        )
    } else {
        PhoneTripScreen(
            state = state,
            onToggleStop = onToggleStop,
            onStartRoute = onStartRoute,
            onMarkArrived = onMarkArrived,
            onIgnition = onIgnition,
            onConfirmNext = onConfirmNext,
            onSkipNext = onSkipNext,
            onResetTrip = onResetTrip,
        )
    }
}

@Composable
fun isCarLayout(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
