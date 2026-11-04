package com.peyya.app.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.viewmodel.TripUiState

@Composable
fun isCarLayout(): Boolean {
    val config = LocalConfiguration.current
    val isCarUiMode =
        (config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_CAR
    return isCarUiMode || config.screenWidthDp >= 960
}

@Composable
fun TripScreen(
    state: TripUiState,
    onToggleStop: (String, Boolean) -> Unit,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
    onSnooze: (SnoozeChoice) -> Unit = {},
    onSnoozeReservation: (Long) -> Unit = {},
    onSkipToday: () -> Unit = {},
    onConfirmArrivalFromPrompt: () -> Unit = {},
    onDismissArrivalConfirm: () -> Unit = {},
) {
    if (isCarLayout()) {
        CarTripScreen(
            state = state,
            onToggleStop = onToggleStop,
            onStartRoute = onStartRoute,
            onMarkArrived = onMarkArrived,
            onIgnition = onIgnition,
            onConfirmNext = onConfirmNext,
            onSkipNext = onSkipNext,
            onResetTrip = onResetTrip,
            onSnooze = onSnooze,
            onSnoozeReservation = onSnoozeReservation,
            onSkipToday = onSkipToday,
            onConfirmArrivalFromPrompt = onConfirmArrivalFromPrompt,
            onDismissArrivalConfirm = onDismissArrivalConfirm,
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
            onSnooze = onSnooze,
            onSnoozeReservation = onSnoozeReservation,
            onSkipToday = onSkipToday,
            onConfirmArrivalFromPrompt = onConfirmArrivalFromPrompt,
            onDismissArrivalConfirm = onDismissArrivalConfirm,
        )
    }
}
