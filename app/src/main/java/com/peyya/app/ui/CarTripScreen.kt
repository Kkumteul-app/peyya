package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.ui.components.DemoBadge
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.components.StopThumb
import com.peyya.app.ui.theme.CarBg
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLine
import com.peyya.app.ui.theme.CarPanel
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.viewmodel.TripUiState

@Composable
fun CarTripScreen(
    state: TripUiState,
    onToggleStop: (String, Boolean) -> Unit,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
) {
    val actions = resolveTripActions(
        state, onStartRoute, onMarkArrived, onIgnition, onConfirmNext, onSkipNext, onResetTrip,
    )
    val (title, subtitle) = heroCopy(state)
    val focusStop = when (val p = state.phase) {
        is TripPhase.Navigating -> state.trip.stops.firstOrNull { it.id == p.stopId }
        is TripPhase.Arrived -> state.trip.stops.firstOrNull { it.id == p.stopId }
        is TripPhase.AwaitingNext -> state.trip.stops.firstOrNull { it.id == p.nextStopId }
        else -> state.trip.selectedStops().firstOrNull()
    }
    val showTts = state.phase is TripPhase.AwaitingNext && state.lastSpoken.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(CarBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        MapPane(
            modifier = Modifier
                .weight(1.35f)
                .fillMaxHeight(),
            phaseLabel = state.phase.labelKo(),
            mock = state.mockMode,
            routeTitle = state.trip.title,
            etaLabel = etaLabel(state),
            etaSub = focusStop?.let { "${it.name}" } ?: state.statusMessage,
        )
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .background(CarPanel)
                .padding(22.dp),
        ) {
            Text(
                state.phase.labelKo(),
                color = CarBrandLight,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(8.dp))
            Text(title, color = CarText, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = CarDim, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(14.dp))
            if (showTts) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CarCard)
                        .border(1.dp, PeyyaBrand, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text(state.lastSpoken, color = CarText, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
            }
            focusStop?.let { stop ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CarCard)
                        .border(1.dp, CarLine, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StopThumb(stop.id, Modifier.size(72.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(stop.name, color = CarText, style = MaterialTheme.typography.titleMedium)
                        Text(state.statusMessage, color = CarDim, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            if (state.phase is TripPhase.Idle) {
                Text("경유지 (탭하여 선택)", color = CarDim, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                state.trip.stops.forEach { stop ->
                    Text(
                        text = "${if (stop.selected) "✓" else "○"}  ${stop.name}",
                        color = if (stop.selected) CarText else CarDim,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarCard)
                            .clickable { onToggleStop(stop.id, !stop.selected) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            PrimaryCta(actions.primaryLabel, actions.primaryEnabled, actions.onPrimary)
            if (actions.secondaryLabel != null && actions.onSecondary != null) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryCta(
                        actions.secondaryLabel,
                        actions.secondaryEnabled,
                        actions.onSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    if (actions.tertiaryLabel != null && actions.onTertiary != null) {
                        SecondaryCta(
                            actions.tertiaryLabel,
                            actions.tertiaryEnabled,
                            actions.onTertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "${state.family.familyName} · ${state.family.members.joinToString(" · ") { it.name }}",
                color = CarDim,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MapPane(
    modifier: Modifier,
    phaseLabel: String,
    mock: Boolean,
    routeTitle: String,
    etaLabel: String,
    etaSub: String,
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(Color(0xFF0D1A30), Color(0xFF132844), Color(0xFF0B1526))),
        ),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = 80.dp)
                .width(110.dp)
                .fillMaxHeight()
                .rotate(12f)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF2A3F63), Color(0xFF3D5A8A), Color(0xFF2A3F63))),
                ),
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .size(18.dp)
                .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp, bottomEnd = 9.dp))
                .rotate(-45f)
                .background(PeyyaBrand),
        )
        Row(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Peyya", color = CarText, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Chip(routeTitle)
                Chip(phaseLabel)
                DemoBadge(visible = mock, onDark = true)
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xD10C1220))
                .border(1.dp, CarLine, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Text("상태", color = CarDim, style = MaterialTheme.typography.labelMedium)
            Text(etaLabel, color = CarText, style = MaterialTheme.typography.headlineMedium)
            Text(etaSub, color = CarDim, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        text,
        color = CarText,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

private fun etaLabel(state: TripUiState): String = when (state.phase) {
    TripPhase.Idle -> "준비"
    is TripPhase.Navigating -> "안내 중"
    is TripPhase.Arrived -> "도착 완료"
    is TripPhase.AwaitingNext -> "확인 대기"
    TripPhase.Completed -> "모두 완료"
}
