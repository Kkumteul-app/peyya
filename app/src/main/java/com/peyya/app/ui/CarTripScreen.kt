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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLine
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarPanel
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaDanger
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
    val showVoice = state.phase is TripPhase.AwaitingNext && state.lastSpoken.isNotBlank()
    var menuExpanded by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }

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
            mock = state.mockMode,
            routeTitle = state.trip.title,
        )
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .background(CarPanel)
                .padding(22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.phase.labelKo(),
                    color = CarBrandLight,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(64.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "더보기",
                            tint = CarTextSub,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("일정 처음부터 다시") },
                            onClick = {
                                menuExpanded = false
                                showReset = true
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(title, color = CarText, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = CarDim, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(14.dp))
            if (showVoice) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CarCard)
                        .border(1.dp, PeyyaBrand, RoundedCornerShape(14.dp))
                        .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        state.lastSpoken,
                        color = CarText,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onIgnition,
                        modifier = Modifier
                            .size(64.dp)
                            .border(1.dp, CarLineStrong, RoundedCornerShape(16.dp)),
                    ) {
                        Icon(
                            Icons.Rounded.Replay,
                            contentDescription = "다시 듣기",
                            tint = CarText,
                            modifier = Modifier.size(28.dp),
                        )
                    }
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
                        Text(stop.address, color = CarDim, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
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
            val notice = state.notice
            if (notice != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = CarTextSub,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(notice, color = CarTextSub, fontSize = 18.sp)
                }
            }
            val primaryLabel = actions.primaryLabel
            if (primaryLabel != null) {
                PrimaryCta(
                    label = primaryLabel,
                    enabled = actions.primaryEnabled,
                    onClick = actions.onPrimary,
                    demo = actions.primaryIsDemo,
                )
            }
            if (actions.secondaryLabel != null && actions.onSecondary != null) {
                Spacer(Modifier.height(8.dp))
                SecondaryCta(
                    label = actions.secondaryLabel,
                    onClick = actions.onSecondary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "${state.family.familyName} · ${state.family.members.joinToString(" · ") { it.name }}",
                color = CarDim,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("일정을 처음부터 다시 할까요?") },
            text = { Text("지금까지 진행한 루트 기록이 지워집니다.") },
            confirmButton = {
                TextButton(onClick = { showReset = false; onResetTrip() }) {
                    Text("다시 하기", color = PeyyaDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun MapPane(
    modifier: Modifier,
    mock: Boolean,
    routeTitle: String,
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
                DemoBadge(visible = mock, onDark = true)
            }
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
