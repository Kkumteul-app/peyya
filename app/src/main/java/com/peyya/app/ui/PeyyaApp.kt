package com.peyya.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peyya.app.ui.review.ReviewFlowCallbacks
import com.peyya.app.ui.theme.PeyyaTheme
import com.peyya.app.viewmodel.TripViewModel
import com.peyya.app.viewmodel.TripViewModelFactory

@Composable
fun PeyyaApp(
    forceMock: Boolean = false,
) {
    val context = LocalContext.current
    val viewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(
            appContext = context.applicationContext,
            forceMock = forceMock,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val carMode = isCarLayout()
    val snackbarHostState = remember { SnackbarHostState() }
    val reviewCallbacks = remember(carMode, viewModel) {
        ReviewFlowCallbacks(
            onBeginReview = viewModel::beginReviewRating,
            onSkipReviewSession = viewModel::skipReviewSession,
            onSetReviewStars = viewModel::setReviewStars,
            onSetReviewComment = viewModel::setReviewComment,
            onCommitReviewStop = { viewModel.commitReviewStop(carMode = carMode) },
            onSkipReviewStop = { viewModel.skipReviewStop(carMode = carMode) },
            onAcknowledgeReviewSubmitted = viewModel::acknowledgeReviewSubmitted,
            onApplyCourse = viewModel::applyCourseOffer,
            onDismissCourse = viewModel::dismissCourseOffer,
            onAcknowledgeCourseApplied = viewModel::acknowledgeCourseApplied,
        )
    }

    LaunchedEffect(state.snackbar) {
        val message = state.snackbar ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSnackbar()
    }

    PeyyaTheme(carMode = carMode) {
        TripScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onToggleStop = viewModel::toggleStop,
            onUpdatePlanSearch = viewModel::updatePlanSearchQuery,
            onSelectPlanAnchor = viewModel::selectPlanAnchor,
            onTogglePlanNearby = viewModel::togglePlanNearby,
            onConfirmPlanEnd = viewModel::confirmPlanEnd,
            onConfirmPlanContinue = viewModel::confirmPlanContinue,
            onSetPlanTiming = viewModel::setPlanTiming,
            onSetPlanCompanion = viewModel::setPlanCompanion,
            onSetPlanDepartAt = viewModel::setPlanDepartAt,
            onConfirmPlanPrefs = viewModel::confirmPlanPrefs,
            onReopenPlanPrefs = viewModel::reopenPlanPrefs,
            onReplacePlanRouteStop = viewModel::replacePlanRouteStop,
            onRemovePlanRouteStop = viewModel::removePlanRouteStop,
            onMovePlanRouteStop = viewModel::movePlanRouteStop,
            onSaveWeekendPlan = viewModel::saveWeekendPlan,
            onFindPlanRoute = viewModel::findPlanRoute,
            onStartRoute = viewModel::startDriving,
            onMarkArrived = viewModel::markArrivedFromMenu,
            onIgnition = viewModel::onIgnition,
            onReplayGuideSpeech = viewModel::replayGuideSpeech,
            onConfirmNext = viewModel::confirmNext,
            onSkipNext = viewModel::skipNext,
            onResetTrip = viewModel::resetTrip,
            onSnooze = viewModel::snooze,
            onSnoozeReservation = viewModel::snoozeReservation,
            onSkipToday = viewModel::skipToday,
            onConfirmArrivalFromPrompt = viewModel::confirmArrivalFromPrompt,
            onDismissArrivalConfirm = viewModel::dismissArrivalConfirm,
            reviewCallbacks = reviewCallbacks,
            onAcceptAdapt = viewModel::acceptAdaptCandidate,
            onDismissAdapt = viewModel::dismissAdaptSuggestion,
            onSimulateVoice = viewModel::simulateVoiceUtterance,
            onUpdateFamilyName = viewModel::updateFamilyName,
            onUpdateDisplayName = viewModel::updateDisplayName,
            onUpdateVehicleModel = viewModel::updateVehicleModel,
            onUpdateVehiclePlate = viewModel::updateVehiclePlate,
        )
    }
}
