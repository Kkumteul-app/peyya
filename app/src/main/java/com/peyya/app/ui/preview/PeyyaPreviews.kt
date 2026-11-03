package com.peyya.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.ui.CarTripScreen
import com.peyya.app.ui.PhoneTripScreen
import com.peyya.app.ui.theme.PeyyaTheme
import com.peyya.app.viewmodel.TripUiState

private val previewTrip = PresetTrips.weekendMuseumTrip
private val previewFamily = PresetTrips.defaultFamily
private const val NEXT_PROMPT = "다음 목적지는 용산 한식 담소입니다. 안내를 시작할까요?"

private fun phoneArrivedState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Arrived("museum"),
    mockMode = true,
    lastSpoken = "",
)

private fun phoneAwaitingState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.AwaitingNext("restaurant"),
    mockMode = true,
    lastSpoken = NEXT_PROMPT,
)

private fun carAwaitingState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.AwaitingNext("restaurant"),
    mockMode = true,
    lastSpoken = NEXT_PROMPT,
)

private fun carIdleState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Idle,
    mockMode = true,
    lastSpoken = "",
)

private val noop: () -> Unit = {}
private val noopToggle: (String, Boolean) -> Unit = { _, _ -> }

@Preview(name = "Phone · 도착", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneArrived() {
    PeyyaTheme(carMode = false) {
        PhoneTripScreen(
            state = phoneArrivedState(),
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
        )
    }
}

@Preview(name = "Phone · 다음 장소", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneAwaiting() {
    PeyyaTheme(carMode = false) {
        PhoneTripScreen(
            state = phoneAwaitingState(),
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
        )
    }
}

@Preview(name = "Car · 다음 장소", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarAwaiting() {
    PeyyaTheme(carMode = true) {
        CarTripScreen(
            state = carAwaitingState(),
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
        )
    }
}

@Preview(name = "Car · 출발 전", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarIdle() {
    PeyyaTheme(carMode = true) {
        CarTripScreen(
            state = carIdleState(),
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
        )
    }
}
