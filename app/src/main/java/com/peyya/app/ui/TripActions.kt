package com.peyya.app.ui

import com.peyya.app.domain.TripPhase
import com.peyya.app.viewmodel.TripUiState

data class TripActions(
    val primaryLabel: String?,
    val primaryEnabled: Boolean,
    val onPrimary: () -> Unit,
    val primaryIsDemo: Boolean = false,
    val secondaryLabel: String? = null,
    val onSecondary: (() -> Unit)? = null,
)

fun resolveTripActions(
    state: TripUiState,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
): TripActions = when (state.phase) {
    TripPhase.Idle -> TripActions(
        primaryLabel = "오늘 루트 시작",
        primaryEnabled = state.trip.selectedStops().isNotEmpty(),
        onPrimary = onStartRoute,
    )
    is TripPhase.Navigating -> TripActions(
        primaryLabel = "도착했어요",
        primaryEnabled = true,
        onPrimary = onMarkArrived,
        primaryIsDemo = state.mockMode,
    )
    is TripPhase.Arrived -> TripActions(
        primaryLabel = "시동 걸기",
        primaryEnabled = true,
        onPrimary = onIgnition,
        primaryIsDemo = state.mockMode,
        secondaryLabel = "다음 장소 건너뛰기",
        onSecondary = onSkipNext,
    )
    is TripPhase.AwaitingNext -> TripActions(
        primaryLabel = "안내 시작",
        primaryEnabled = true,
        onPrimary = onConfirmNext,
        secondaryLabel = "건너뛰기",
        onSecondary = onSkipNext,
    )
    TripPhase.Completed -> TripActions(
        primaryLabel = "처음부터 다시",
        primaryEnabled = true,
        onPrimary = onResetTrip,
    )
}

fun heroCopy(state: TripUiState): Pair<String, String> = when (val phase = state.phase) {
    TripPhase.Idle -> "오늘 이 루트로 갈까요?" to
        "내비는 길을, 페이야는 가족의 다음 장소를 이어 줍니다."
    is TripPhase.Navigating -> {
        val name = state.trip.stops.firstOrNull { it.id == phase.stopId }?.name ?: "목적지"
        "${name}으로 안내 중" to "시스템 내비에 목적지를 전달했습니다."
    }
    is TripPhase.Arrived -> "도착했어요.\n시동 걸면 다음을 부를게요." to
        "재검색 없이 다음 장소로 이어집니다."
    is TripPhase.AwaitingNext -> "다음 장소 확인" to
        "확인하면 내비로 넘기고, 건너뛰면 이 스톱을 제외합니다."
    TripPhase.Completed -> "오늘 루트 끝." to
        "다음 주말도 이 루트를 이어 갈 수 있어요."
}
