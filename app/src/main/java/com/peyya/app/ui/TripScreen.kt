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
    onTogglePrep: (String) -> Unit = {},
    onDismissAdapt: () -> Unit = {},
    onAcceptParking: (String) -> Unit = {},
    onVoiceChip: (String) -> Unit = {},
    onContinueReviewIntro: () -> Unit = {},
    onUpdateReview: (Int, String) -> Unit = { _, _ -> },
    onNextReview: () -> Unit = {},
    onSkipReview: () -> Unit = {},
    onApplyCourse: (String) -> Unit = {},
) {
    val landscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
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
            onTogglePrep = onTogglePrep,
            onDismissAdapt = onDismissAdapt,
            onAcceptParking = onAcceptParking,
            onVoiceChip = onVoiceChip,
            onContinueReviewIntro = onContinueReviewIntro,
            onUpdateReview = onUpdateReview,
            onNextReview = onNextReview,
            onSkipReview = onSkipReview,
            onApplyCourse = onApplyCourse,
        )
    }
}

@Composable
fun isCarLayout(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
