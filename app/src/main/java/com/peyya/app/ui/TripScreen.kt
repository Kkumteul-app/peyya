package com.peyya.app.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.ui.review.ReviewFlowCallbacks
import com.peyya.app.viewmodel.PlanCompanion
import com.peyya.app.viewmodel.PlanTiming
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
    snackbarHostState: SnackbarHostState,
    onToggleStop: (String, Boolean) -> Unit,
    onUpdatePlanSearch: (String) -> Unit = {},
    onSelectPlanAnchor: (String) -> Unit = {},
    onTogglePlanNearby: (String, Boolean) -> Unit = { _, _ -> },
    onConfirmPlanEnd: () -> Unit = {},
    onConfirmPlanContinue: () -> Unit = {},
    onSetPlanTiming: (PlanTiming) -> Unit = {},
    onSetPlanCompanion: (PlanCompanion) -> Unit = {},
    onSetPlanDepartAt: (Long) -> Unit = {},
    onConfirmPlanPrefs: () -> Unit = {},
    onReopenPlanPrefs: () -> Unit = {},
    onReplacePlanRouteStop: (String, String) -> Unit = { _, _ -> },
    onRemovePlanRouteStop: (String) -> Unit = {},
    onMovePlanRouteStop: (String, Int) -> Unit = { _, _ -> },
    onSaveWeekendPlan: () -> Unit = {},
    onFindPlanRoute: () -> Unit = {},
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onReplayGuideSpeech: () -> Unit = {},
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
    onSnooze: (SnoozeChoice) -> Unit = {},
    onSnoozeReservation: (Long) -> Unit = {},
    onSkipToday: () -> Unit = {},
    onConfirmArrivalFromPrompt: () -> Unit = {},
    onDismissArrivalConfirm: () -> Unit = {},
    reviewCallbacks: ReviewFlowCallbacks = ReviewFlowCallbacks(),
    onAcceptAdapt: (String) -> Unit = {},
    onDismissAdapt: () -> Unit = {},
    onSimulateVoice: (String) -> Unit = {},
    onUpdateFamilyName: (String) -> Unit = {},
    onUpdateDisplayName: (String) -> Unit = {},
    onUpdateVehicleModel: (String) -> Unit = {},
    onUpdateVehiclePlate: (String) -> Unit = {},
) {
    if (isCarLayout()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CarTripScreen(
                state = state,
                onToggleStop = onToggleStop,
                onStartRoute = onStartRoute,
                onMarkArrived = onMarkArrived,
                onIgnition = onIgnition,
                onReplayGuideSpeech = onReplayGuideSpeech,
                onConfirmNext = onConfirmNext,
                onSkipNext = onSkipNext,
                onResetTrip = onResetTrip,
                onSnooze = onSnooze,
                onSnoozeReservation = onSnoozeReservation,
                onSkipToday = onSkipToday,
                onConfirmArrivalFromPrompt = onConfirmArrivalFromPrompt,
                onDismissArrivalConfirm = onDismissArrivalConfirm,
                reviewCallbacks = reviewCallbacks,
                onAcceptAdapt = onAcceptAdapt,
                onDismissAdapt = onDismissAdapt,
                onSimulateVoice = onSimulateVoice,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    } else {
        PhoneShell(
            state = state,
            snackbarHostState = snackbarHostState,
            onToggleStop = onToggleStop,
            onUpdatePlanSearch = onUpdatePlanSearch,
            onSelectPlanAnchor = onSelectPlanAnchor,
            onTogglePlanNearby = onTogglePlanNearby,
            onConfirmPlanEnd = onConfirmPlanEnd,
            onConfirmPlanContinue = onConfirmPlanContinue,
            onSetPlanTiming = onSetPlanTiming,
            onSetPlanCompanion = onSetPlanCompanion,
            onSetPlanDepartAt = onSetPlanDepartAt,
            onConfirmPlanPrefs = onConfirmPlanPrefs,
            onReopenPlanPrefs = onReopenPlanPrefs,
            onReplacePlanRouteStop = onReplacePlanRouteStop,
            onRemovePlanRouteStop = onRemovePlanRouteStop,
            onMovePlanRouteStop = onMovePlanRouteStop,
            onSaveWeekendPlan = onSaveWeekendPlan,
            onFindPlanRoute = onFindPlanRoute,
            onStartRoute = onStartRoute,
            onMarkArrived = onMarkArrived,
            onIgnition = onIgnition,
            onReplayGuideSpeech = onReplayGuideSpeech,
            onConfirmNext = onConfirmNext,
            onSkipNext = onSkipNext,
            onResetTrip = onResetTrip,
            onSnooze = onSnooze,
            onSnoozeReservation = onSnoozeReservation,
            onSkipToday = onSkipToday,
            onConfirmArrivalFromPrompt = onConfirmArrivalFromPrompt,
            onDismissArrivalConfirm = onDismissArrivalConfirm,
            reviewCallbacks = reviewCallbacks,
            onAcceptAdapt = onAcceptAdapt,
            onDismissAdapt = onDismissAdapt,
            onSimulateVoice = onSimulateVoice,
            onUpdateFamilyName = onUpdateFamilyName,
            onUpdateDisplayName = onUpdateDisplayName,
            onUpdateVehicleModel = onUpdateVehicleModel,
            onUpdateVehiclePlate = onUpdateVehiclePlate,
        )
    }
}
