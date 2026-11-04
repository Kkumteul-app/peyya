package com.peyya.app.ui

import android.location.Location
import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPhase
import com.peyya.app.viewmodel.TripUiState
import kotlin.math.roundToInt

data class TripActions(
    val primaryLabel: String?,
    val primaryEnabled: Boolean,
    val onPrimary: () -> Unit,
    val primaryIsDemo: Boolean = false,
    val secondaryLabel: String? = null,
    val onSecondary: (() -> Unit)? = null,
)

@Suppress("UNUSED_PARAMETER")
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
        primaryLabel = "운전 시작",
        primaryEnabled = state.trip.selectedStops().isNotEmpty(),
        onPrimary = onStartRoute,
    )
    is TripPhase.Navigating -> TripActions(
        // 도착은 내비/GPS·메뉴「도착으로 표시」. 주 CTA에 「도착했어요」 없음.
        primaryLabel = null,
        primaryEnabled = false,
        onPrimary = {},
    )
    is TripPhase.Arrived -> TripActions(
        // NextLeg는 시동 OFF→ON 또는 유지 5분으로 자동 제안. 상시 「시동 걸기」 없음.
        primaryLabel = null,
        primaryEnabled = false,
        onPrimary = {},
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
    TripPhase.Idle ->
        "오늘은 이 순서로 다녀요" to
            "들를 곳을 고르고 출발하세요. 도착하면 다음 장소를 알려드려요."
    is TripPhase.Navigating -> {
        val name = state.trip.stops.firstOrNull { it.id == phase.stopId }?.name ?: "목적지"
        "${name}${particleRo(name)} 가는 중" to "길안내는 차량 내비가 맡아요."
    }
    is TripPhase.Arrived -> {
        val name = state.trip.stops.firstOrNull { it.id == phase.stopId }?.name ?: "목적지"
        "${name}에\n도착했어요" to
            "시동을 끄었다가 다시 켜거나, 시동을 유지한 채 5분이 지나면 다음 장소를 제안해요."
    }
    is TripPhase.AwaitingNext -> {
        val name = state.trip.stops.firstOrNull { it.id == phase.nextStopId }?.name ?: "다음 장소"
        "${name}${particleRo(name)}\n안내할까요?" to "안내를 시작하면 차량 내비로 넘겨요."
    }
    TripPhase.Completed ->
        "오늘 루트를 모두 다녀왔어요" to
            "다음 주말에도 이 루트로 시작할 수 있어요."
}

/** 「으로/로」 — 받침 없음·ㄹ(8)은 「로」, 그 외 「으로」 */
fun particleRo(name: String): String {
    val last = name.lastOrNull() ?: return "로"
    if (last !in '가'..'힣') return "로"
    val jong = (last.code - 0xAC00) % 28
    return if (jong == 0 || jong == 8) "로" else "으로"
}

fun shortAddress(address: String): String {
    val parts = address.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (parts.size <= 2) address else parts.drop(2).joinToString(" ")
}

fun straightDistanceLabel(from: Stop, to: Stop): String {
    val results = FloatArray(1)
    Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
    val meters = results[0]
    return if (meters >= 1000f) {
        String.format(java.util.Locale.US, "직선 %.1fkm", meters / 1000f)
    } else {
        "직선 ${meters.roundToInt()}m"
    }
}

fun nextStopAfterArrived(state: TripUiState): Stop? {
    val phase = state.phase as? TripPhase.Arrived ?: return null
    val selected = state.trip.selectedStops()
    val idx = selected.indexOfFirst { it.id == phase.stopId }
    return selected.getOrNull(idx + 1)
}

fun doneCount(state: TripUiState): Int {
    val selected = state.trip.selectedStops()
    return when (val phase = state.phase) {
        is TripPhase.Navigating -> selected.indexOfFirst { it.id == phase.stopId }.coerceAtLeast(0)
        is TripPhase.Arrived -> selected.indexOfFirst { it.id == phase.stopId }.coerceAtLeast(0) + 1
        is TripPhase.AwaitingNext -> selected.indexOfFirst { it.id == phase.nextStopId }.coerceAtLeast(0)
        TripPhase.Completed -> selected.size
        else -> 0
    }
}

fun currentStop(state: TripUiState): Stop? = when (val phase = state.phase) {
    is TripPhase.Navigating -> state.trip.stops.firstOrNull { it.id == phase.stopId }
    is TripPhase.Arrived -> state.trip.stops.firstOrNull { it.id == phase.stopId }
    is TripPhase.AwaitingNext -> state.trip.stops.firstOrNull { it.id == phase.nextStopId }
    else -> null
}
