package com.peyya.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

    PeyyaTheme(carMode = carMode) {
        TripScreen(
            state = state,
            onFamilyNameChange = viewModel::updateFamilyName,
            onToggleStop = viewModel::toggleStop,
            onStartRoute = viewModel::startRoute,
            onMarkArrived = viewModel::markArrived,
            onIgnition = viewModel::onIgnition,
            onConfirmNext = viewModel::confirmNext,
            onSkipNext = viewModel::skipNext,
            onResetTrip = viewModel::resetTrip,
        )
    }
}
