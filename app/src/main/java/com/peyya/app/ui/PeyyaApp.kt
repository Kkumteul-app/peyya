package com.peyya.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbar) {
        val message = state.snackbar ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSnackbar()
    }

    PeyyaTheme(carMode = carMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            TripScreen(
                state = state,
                onToggleStop = viewModel::toggleStop,
                onStartRoute = viewModel::startRoute,
                onMarkArrived = viewModel::markArrived,
                onIgnition = viewModel::onIgnition,
                onConfirmNext = viewModel::confirmNext,
                onSkipNext = viewModel::skipNext,
                onResetTrip = viewModel::resetTrip,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
