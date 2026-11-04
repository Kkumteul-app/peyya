package com.peyya.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.progress.SnoozeChoice
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
)

private fun phoneAwaitingState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.AwaitingNext("restaurant"),
    mockMode = true,
    lastSpoken = NEXT_PROMPT,
)

private fun phoneIgnitionRemindState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Idle,
    mockMode = true,
    ignitionPromptVisible = true,
)

private fun phoneGpsAlertState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Navigating("museum"),
    mockMode = true,
    arrivalConfirmVisible = true,
    notice = "도착 완료가 확인되지 않았어요. 도착했다면 알려 주세요.",
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
)

private fun carIgnitionRemindState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Idle,
    mockMode = true,
    ignitionPromptVisible = true,
)

private fun carGpsAlertState() = TripUiState(
    family = previewFamily,
    trip = previewTrip,
    phase = TripPhase.Navigating("museum"),
    mockMode = true,
    arrivalConfirmVisible = true,
)

private val noop: () -> Unit = {}
private val noopToggle: (String, Boolean) -> Unit = { _, _ -> }
private val noopSnooze: (SnoozeChoice) -> Unit = {}
private val noopReservation: (Long) -> Unit = {}

@Composable
private fun PreviewPhone(
    state: TripUiState,
) {
    PeyyaTheme(carMode = false) {
        PhoneTripScreen(
            state = state,
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
            onSnooze = noopSnooze,
            onSnoozeReservation = noopReservation,
            onSkipToday = noop,
            onConfirmArrivalFromPrompt = noop,
            onDismissArrivalConfirm = noop,
        )
    }
}

@Composable
private fun PreviewCar(
    state: TripUiState,
) {
    PeyyaTheme(carMode = true) {
        CarTripScreen(
            state = state,
            onToggleStop = noopToggle,
            onStartRoute = noop,
            onMarkArrived = noop,
            onIgnition = noop,
            onConfirmNext = noop,
            onSkipNext = noop,
            onResetTrip = noop,
            onSnooze = noopSnooze,
            onSnoozeReservation = noopReservation,
            onSkipToday = noop,
            onConfirmArrivalFromPrompt = noop,
            onDismissArrivalConfirm = noop,
        )
    }
}

@Preview(name = "Phone · 도착", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneArrived() = PreviewPhone(phoneArrivedState())

@Preview(name = "Phone · 다음 장소", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneAwaiting() = PreviewPhone(phoneAwaitingState())

@Preview(name = "Phone · 시동 리마인드", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneIgnitionRemind() = PreviewPhone(phoneIgnitionRemindState())

@Preview(name = "Phone · GPS 알림", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PreviewPhoneGpsAlert() = PreviewPhone(phoneGpsAlertState())

@Preview(name = "Car · 다음 장소", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarAwaiting() = PreviewCar(carAwaitingState())

@Preview(name = "Car · 출발 전", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarIdle() = PreviewCar(carIdleState())

@Preview(name = "Car · 시동 리마인드", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarIgnitionRemind() = PreviewCar(carIgnitionRemindState())

@Preview(name = "Car · GPS 알림", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun PreviewCarGpsAlert() = PreviewCar(carGpsAlertState())
