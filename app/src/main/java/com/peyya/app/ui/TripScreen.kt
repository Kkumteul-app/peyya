package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.ui.theme.PeyyaMockBanner
import com.peyya.app.viewmodel.TripUiState

@Composable
fun TripScreen(
    state: TripUiState,
    onFamilyNameChange: (String) -> Unit,
    onToggleStop: (String, Boolean) -> Unit,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp, vertical = 20.dp),
    ) {
        if (state.mockMode) {
            MockBanner()
            Spacer(Modifier.height(12.dp))
        }
        Text("Peyya", style = MaterialTheme.typography.headlineLarge)
        Text("주말 멀티스톱 패밀리 일정 OS", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LeftPane(
                state = state,
                onFamilyNameChange = onFamilyNameChange,
                onToggleStop = onToggleStop,
                modifier = Modifier.weight(1.1f),
            )
            RightPane(
                state = state,
                onStartRoute = onStartRoute,
                onMarkArrived = onMarkArrived,
                onIgnition = onIgnition,
                onConfirmNext = onConfirmNext,
                onSkipNext = onSkipNext,
                onResetTrip = onResetTrip,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MockBanner() {
    Text(
        text = "Mock 모드 — Pleos SDK 없이 폰 데모 / 영상 촬영용",
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(PeyyaMockBanner)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun LeftPane(
    state: TripUiState,
    onFamilyNameChange: (String) -> Unit,
    onToggleStop: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("가족 프로필", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = state.family.familyName,
            onValueChange = onFamilyNameChange,
            label = { Text("가족 이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        state.family.members.forEach { member ->
            Text(
                "${member.name} · ${member.ageBand.labelKo}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(state.trip.title, style = MaterialTheme.typography.titleLarge)
        Text("경유지 (체크 후 시작)", style = MaterialTheme.typography.bodyMedium)
        state.trip.stops.forEach { stop ->
            StopRow(stop = stop, enabled = state.phase is TripPhase.Idle, onToggle = onToggleStop)
        }
    }
}

@Composable
private fun StopRow(
    stop: Stop,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = stop.selected,
            onCheckedChange = { checked -> onToggle(stop.id, checked) },
            enabled = enabled,
        )
        Column {
            Text(stop.name, style = MaterialTheme.typography.titleMedium)
            Text(stop.address, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RightPane(
    state: TripUiState,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLabel = currentStopLabel(state)
    val ignitionEnabled =
        state.phase is TripPhase.Arrived || state.phase is TripPhase.AwaitingNext
    val skipEnabled =
        state.phase is TripPhase.Arrived || state.phase is TripPhase.AwaitingNext
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("진행 상태", style = MaterialTheme.typography.titleLarge)
        Text("단계: ${state.phase.labelKo()}", style = MaterialTheme.typography.titleMedium)
        Text("현재: $currentLabel", style = MaterialTheme.typography.bodyLarge)
        Text(state.statusMessage, style = MaterialTheme.typography.bodyLarge)
        if (state.lastSpoken.isNotBlank()) {
            Text("TTS: ${state.lastSpoken}", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onStartRoute, enabled = state.phase is TripPhase.Idle) {
            Text("오늘 루트 시작")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onMarkArrived,
                enabled = state.phase is TripPhase.Navigating,
            ) { Text("도착 처리") }
            Button(onClick = onIgnition, enabled = ignitionEnabled) {
                Text("시동 (다음 안내)")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onConfirmNext,
                enabled = state.phase is TripPhase.AwaitingNext,
            ) { Text("다음 안내 확인") }
            OutlinedButton(onClick = onSkipNext, enabled = skipEnabled) {
                Text("건너뛰기")
            }
        }
        OutlinedButton(
            onClick = onResetTrip,
            enabled = state.phase !is TripPhase.Idle,
        ) { Text("일정 초기화 (재촬영)") }
        Spacer(Modifier.width(1.dp))
        Text(
            "예약·결제는 데모 스텁입니다 (실 API 없음).",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun currentStopLabel(state: TripUiState): String {
    val stopId = when (val phase = state.phase) {
        is TripPhase.Navigating -> phase.stopId
        is TripPhase.Arrived -> phase.stopId
        is TripPhase.AwaitingNext -> phase.nextStopId
        else -> null
    } ?: return "—"
    return state.trip.stops.firstOrNull { it.id == stopId }?.name ?: "—"
}
