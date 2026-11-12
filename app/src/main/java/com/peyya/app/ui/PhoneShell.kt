package com.peyya.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.ui.review.ReviewFlowCallbacks
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.viewmodel.PlanCompanion
import com.peyya.app.viewmodel.PlanTiming
import com.peyya.app.viewmodel.TripUiState

@Composable
fun PhoneShell(
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
    var tab by rememberSaveable { mutableStateOf(PhoneTab.Plan.name) }
    val selected = PhoneTab.entries.firstOrNull { it.name == tab } ?: PhoneTab.Plan

    LaunchedEffect(state.review?.sessionId) {
        if (state.review != null) tab = PhoneTab.Execute.name
    }
    LaunchedEffect(state.courseAppliedConfirm) {
        if (state.courseAppliedConfirm) tab = PhoneTab.Plan.name
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PeyyaCanvas,
        bottomBar = {
            PhoneBottomBar(
                selected = selected,
                onSelect = { tab = it.name },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        },
    ) { padding ->
        when (selected) {
            PhoneTab.Plan -> PhonePlanScreen(
                state = state,
                contentPadding = padding,
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
                onGoExecute = { tab = PhoneTab.Execute.name },
            )
            PhoneTab.Execute -> PhoneTripScreen(
                state = state,
                contentPadding = padding,
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
            PhoneTab.Recommend -> PhoneRecommendScreen(
                state = state,
                contentPadding = padding,
            )
            PhoneTab.My -> PhoneMyScreen(
                state = state,
                contentPadding = padding,
                onUpdateDisplayName = onUpdateDisplayName,
                onUpdateVehicleModel = onUpdateVehicleModel,
                onUpdateVehiclePlate = onUpdateVehiclePlate,
                onUpdateFamilyName = onUpdateFamilyName,
                onResetTrip = onResetTrip,
                onGoPlan = { tab = PhoneTab.Plan.name },
            )
        }
    }
}
