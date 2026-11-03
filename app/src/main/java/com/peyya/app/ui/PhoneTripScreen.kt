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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.ui.components.FamilyAvatars
import com.peyya.app.ui.components.MockChip
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.components.StopCard
import com.peyya.app.ui.theme.PeyyaBrand
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 16.dp),
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
            MockChip(state.mockMode)
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
                if (state.lastSpoken.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("TTS: ${state.lastSpoken}", color = PeyyaBrand, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(state.statusMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        PrimaryCta(actions.primaryLabel, actions.primaryEnabled, actions.onPrimary)
        if (actions.secondaryLabel != null && actions.onSecondary != null) {
            Spacer(Modifier.height(8.dp))
            SecondaryCta(actions.secondaryLabel, actions.secondaryEnabled, actions.onSecondary)
        }
        if (actions.tertiaryLabel != null && actions.onTertiary != null) {
            Spacer(Modifier.height(8.dp))
            SecondaryCta(actions.tertiaryLabel, actions.tertiaryEnabled, actions.onTertiary, outlined = false)
        }
    }
}
