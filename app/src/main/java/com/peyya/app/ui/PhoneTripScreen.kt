package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.ui.components.DemoBadge
import com.peyya.app.ui.components.FamilyAvatars
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.components.StopCard
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaDanger
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.viewmodel.TripUiState

@Composable
fun PhoneTripScreen(
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
    val currentId = when (val p = state.phase) {
        is TripPhase.Navigating -> p.stopId
        is TripPhase.Arrived -> p.stopId
        is TripPhase.AwaitingNext -> p.nextStopId
        else -> null
    }
    val selected = state.trip.selectedStops()
    val doneIds = when (val p = state.phase) {
        is TripPhase.Navigating -> selected.takeWhile { it.id != p.stopId }.map { it.id }.toSet()
        is TripPhase.Arrived -> selected.takeWhile { it.id != p.stopId }.map { it.id }.toSet() + p.stopId
        is TripPhase.AwaitingNext -> {
            val idx = selected.indexOfFirst { it.id == p.nextStopId }
            if (idx <= 0) emptySet() else selected.take(idx).map { it.id }.toSet()
        }
        TripPhase.Completed -> selected.map { it.id }.toSet()
        else -> emptySet()
    }
    val showVoice = state.phase is TripPhase.AwaitingNext && state.lastSpoken.isNotBlank()
    var menuExpanded by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Peyya", style = MaterialTheme.typography.titleLarge)
                Text(state.phase.labelKo(), color = PeyyaBrand, style = MaterialTheme.typography.labelMedium)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DemoBadge(visible = state.mockMode, onDark = false)
                BoxWithMenu(
                    menuExpanded = menuExpanded,
                    onExpand = { menuExpanded = true },
                    onDismiss = { menuExpanded = false },
                    onResetClick = {
                        menuExpanded = false
                        showReset = true
                    },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("오늘 루트", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(state.family.familyName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            FamilyAvatars(state.family.members)
            Spacer(Modifier.height(16.dp))
            Text(state.trip.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            state.trip.stops.forEachIndexed { index, stop ->
                StopCard(
                    stop = stop,
                    index = index,
                    selected = stop.selected,
                    active = stop.id == currentId,
                    done = stop.id in doneIds || (state.phase is TripPhase.Completed && stop.selected),
                    enabled = state.phase is TripPhase.Idle,
                    onToggle = onToggleStop,
                )
                Spacer(Modifier.height(10.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PeyyaSoft)
                    .padding(14.dp),
            ) {
                Text(title.replace('\n', ' '), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (showVoice) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            state.lastSpoken,
                            color = PeyyaBrand,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onIgnition, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Rounded.Replay,
                                contentDescription = "다시 듣기",
                                tint = PeyyaInk,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
            val notice = state.notice
            if (notice != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = PeyyaInkSub,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(notice, color = PeyyaInkSub, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
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
            Spacer(Modifier.height(4.dp))
            SecondaryCta(actions.secondaryLabel, actions.onSecondary)
        }
    }

    if (showReset) {
        ResetConfirmDialog(
            onDismiss = { showReset = false },
            onConfirm = {
                showReset = false
                onResetTrip()
            },
        )
    }
}

@Composable
private fun BoxWithMenu(
    menuExpanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onResetClick: () -> Unit,
) {
    Box {
        IconButton(onClick = onExpand, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "더보기",
                tint = PeyyaInk,
                modifier = Modifier.size(22.dp),
            )
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismiss) {
            DropdownMenuItem(
                text = { Text("일정 처음부터 다시") },
                onClick = onResetClick,
            )
        }
    }
}

@Composable
private fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("일정을 처음부터 다시 할까요?") },
        text = { Text("지금까지 진행한 루트 기록이 지워집니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("다시 하기", color = PeyyaDanger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}
