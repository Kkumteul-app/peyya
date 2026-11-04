package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.ui.components.ArrivalConfirmBanner
import com.peyya.app.ui.components.CarOutlinedCta
import com.peyya.app.ui.components.CarStopSelectRow
import com.peyya.app.ui.components.CarStopTimelineRow
import com.peyya.app.ui.components.DemoBadge
import com.peyya.app.ui.components.IgnitionReminderActions
import com.peyya.app.ui.components.IgnitionReminderCard
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SnoozePicker
import com.peyya.app.ui.components.StopState
import com.peyya.app.ui.theme.CarBg
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLine
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarPanel
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
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
    onSnooze: (SnoozeChoice) -> Unit = {},
    onSnoozeReservation: (Long) -> Unit = {},
    onSkipToday: () -> Unit = {},
    onConfirmArrivalFromPrompt: () -> Unit = {},
    onDismissArrivalConfirm: () -> Unit = {},
) {
    val actions = resolveTripActions(
        state, onStartRoute, onMarkArrived, onIgnition, onConfirmNext, onSkipNext, onResetTrip,
    )
    val (title, _) = heroCopy(state)
    val showVoice = state.phase is TripPhase.AwaitingNext && state.lastSpoken.isNotBlank()
    val addressLine = carAddressLine(state)
    var menuExpanded by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var showSnooze by remember { mutableStateOf(false) }
    LaunchedEffect(state.ignitionPromptVisible) {
        if (!state.ignitionPromptVisible) showSnooze = false
    }
    val leftSection = when {
        showSnooze -> "오늘 루트 · 나중에 확인"
        state.ignitionPromptVisible -> "오늘 루트 · 시동 리마인드"
        state.arrivalConfirmVisible -> "오늘 루트 · 안내 중"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(CarBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        RouteTimelinePane(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight(),
            state = state,
            onToggleStop = onToggleStop,
            sectionOverride = leftSection,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(CarPanel)
                .padding(horizontal = 56.dp, vertical = 48.dp),
        ) {
            when {
                showSnooze -> {
                    SnoozePicker(
                        onSnooze = onSnooze,
                        onReservation = {
                            onSnoozeReservation(System.currentTimeMillis() + 2 * 60 * 60_000L)
                        },
                        onDismiss = { showSnooze = false },
                        carMode = true,
                    )
                }
                state.arrivalConfirmVisible -> {
                    ArrivalConfirmBanner(
                        onConfirmArrival = onConfirmArrivalFromPrompt,
                        onDismiss = onDismissArrivalConfirm,
                        carMode = true,
                    )
                }
                state.ignitionPromptVisible -> {
                    IgnitionReminderCard(carMode = true)
                    Spacer(Modifier.weight(1f))
                    IgnitionReminderActions(
                        onStartDriving = onStartRoute,
                        onSnoozeLater = { showSnooze = true },
                        onSkipToday = onSkipToday,
                        startEnabled = state.trip.selectedStops().isNotEmpty(),
                        carMode = true,
                    )
                }
                else -> {
                    CarDefaultRightPane(
                        state = state,
                        title = title,
                        addressLine = addressLine,
                        showVoice = showVoice,
                        actions = actions,
                        menuExpanded = menuExpanded,
                        onMenuExpand = { menuExpanded = true },
                        onMenuDismiss = { menuExpanded = false },
                        onReset = {
                            menuExpanded = false
                            showReset = true
                        },
                        onMarkArrived = {
                            menuExpanded = false
                            onMarkArrived()
                        },
                        onIgnition = onIgnition,
                    )
                }
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("일정을 처음부터 다시 할까요?", fontSize = 20.sp) },
            text = { Text("지금까지 진행한 루트 기록이 지워집니다.", fontSize = 18.sp) },
            confirmButton = {
                TextButton(onClick = { showReset = false; onResetTrip() }) {
                    Text("다시 하기", color = PeyyaDanger, fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) {
                    Text("취소", fontSize = 18.sp)
                }
            },
        )
    }
}

@Composable
private fun CarDefaultRightPane(
    state: TripUiState,
    title: String,
    addressLine: String?,
    showVoice: Boolean,
    actions: TripActions,
    menuExpanded: Boolean,
    onMenuExpand: () -> Unit,
    onMenuDismiss: () -> Unit,
    onReset: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            state.phase.labelKo(),
            color = CarBrandLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Box {
            IconButton(onClick = onMenuExpand, modifier = Modifier.size(64.dp)) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "더보기",
                    tint = CarTextSub,
                    modifier = Modifier.size(28.dp),
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onMenuDismiss) {
                DropdownMenuItem(
                    text = { Text("일정 처음부터 다시", fontSize = 18.sp) },
                    onClick = onReset,
                )
                if (state.phase is TripPhase.Navigating) {
                    DropdownMenuItem(
                        text = { Text("도착으로 표시", fontSize = 18.sp) },
                        onClick = onMarkArrived,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Text(
        title,
        color = CarText,
        style = MaterialTheme.typography.displaySmall,
    )
    if (addressLine != null) {
        Spacer(Modifier.height(20.dp))
        Text(
            addressLine,
            color = CarTextSub,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    if (showVoice) {
        Spacer(Modifier.height(36.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CarCard)
                .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
                tint = CarBrandLight,
                modifier = Modifier.size(28.dp),
            )
            Text(
                "「${state.lastSpoken}」",
                color = CarText,
                style = MaterialTheme.typography.bodyMedium,
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
    }
    Spacer(Modifier.weight(1f))
    val notice = state.notice
    if (notice != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = CarTextSub,
                modifier = Modifier.size(22.dp),
            )
            Text(notice, color = CarTextSub, fontSize = 18.sp)
        }
    }
    val primaryLabel = actions.primaryLabel
    val secondaryLabel = actions.secondaryLabel
    val onSecondary = actions.onSecondary
    when {
        primaryLabel != null && secondaryLabel != null && onSecondary != null -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PrimaryCta(
                    label = primaryLabel,
                    enabled = actions.primaryEnabled,
                    onClick = actions.onPrimary,
                    demo = actions.primaryIsDemo,
                    modifier = Modifier.weight(2f),
                    height = 84.dp,
                    corner = 20.dp,
                    textSize = 26.sp,
                    textWeight = FontWeight.ExtraBold,
                )
                CarOutlinedCta(
                    label = secondaryLabel,
                    onClick = onSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        primaryLabel != null -> {
            PrimaryCta(
                label = primaryLabel,
                enabled = actions.primaryEnabled,
                onClick = actions.onPrimary,
                demo = actions.primaryIsDemo,
                height = 84.dp,
                corner = 20.dp,
                textSize = 26.sp,
                textWeight = FontWeight.ExtraBold,
            )
        }
        secondaryLabel != null && onSecondary != null -> {
            CarOutlinedCta(
                label = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    }
}

@Composable
private fun RouteTimelinePane(
    modifier: Modifier,
    state: TripUiState,
    onToggleStop: (String, Boolean) -> Unit,
    sectionOverride: String? = null,
) {
    val selected = state.trip.selectedStops()
    val done = doneCount(state)
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
    val focusId = when (val p = state.phase) {
        is TripPhase.Navigating -> p.stopId
        is TripPhase.AwaitingNext -> p.nextStopId
        is TripPhase.Arrived -> nextStopAfterArrived(state)?.id
        else -> null
    }
    val membersLine = state.family.members.joinToString(" · ") { member ->
        when (member.ageBand) {
            AgeBand.CHILD, AgeBand.SENIOR -> "${member.name}(${member.ageBand.labelKo})"
            else -> member.name
        }
    }

    Box(modifier = modifier.background(CarBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "페이야",
                    color = CarText,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.width(10.dp))
                DemoBadge(visible = state.mockMode, onDark = true)
            }
            Column(Modifier.weight(1f)) {
                if (state.phase !is TripPhase.Idle || sectionOverride != null) {
                    val header = sectionOverride
                        ?: "오늘 루트 · $done / ${selected.size} 완료"
                    Text(
                        header,
                        color = CarTextSub,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(20.dp))
                    if (state.phase is TripPhase.Idle && sectionOverride != null) {
                        selected.forEachIndexed { index, stop ->
                            CarStopTimelineRow(
                                order = index + 1,
                                name = stop.name,
                                caption = if (index == 0) "첫 목적지" else "대기",
                                state = if (index == 0) StopState.Current else StopState.Upcoming,
                                isLast = index == selected.lastIndex,
                            )
                        }
                    } else if (state.phase !is TripPhase.Idle) {
                        selected.forEachIndexed { index, stop ->
                            val stopState = when {
                                stop.id in doneIds -> StopState.Done
                                stop.id == focusId -> StopState.Current
                                else -> StopState.Upcoming
                            }
                            val caption = when (stopState) {
                                StopState.Done -> "다녀왔어요"
                                StopState.Current -> when (state.phase) {
                                    is TripPhase.Navigating -> "지금 가는 곳"
                                    else -> "다음 장소"
                                }
                                StopState.Upcoming -> ""
                            }
                            CarStopTimelineRow(
                                order = index + 1,
                                name = stop.name,
                                caption = caption,
                                state = stopState,
                                isLast = index == selected.lastIndex,
                            )
                        }
                    }
                } else {
                    Text(
                        "오늘 루트 · 경유지 선택",
                        color = CarTextSub,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(20.dp))
                    state.trip.stops.forEach { stop ->
                        CarStopSelectRow(stop = stop, onToggle = onToggleStop)
                    }
                }
            }
            Column {
                Text(
                    "${state.family.familyName} ${state.family.members.size}명",
                    color = CarTextSub,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(membersLine, color = CarDim, fontSize = 18.sp)
            }
        }
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(1.dp)
                .fillMaxHeight()
                .background(CarLine),
        )
    }
}

private fun carAddressLine(state: TripUiState): String? = when (val phase = state.phase) {
    is TripPhase.Arrived -> {
        val next = nextStopAfterArrived(state) ?: return null
        val from = state.trip.stops.firstOrNull { it.id == phase.stopId }
        val dist = from?.let { straightDistanceLabel(it, next) }
        listOfNotNull(shortAddress(next.address), dist).joinToString(" · ")
    }
    is TripPhase.AwaitingNext -> {
        val stop = state.trip.stops.firstOrNull { it.id == phase.nextStopId } ?: return null
        val prev = state.trip.selectedStops().let { list ->
            val idx = list.indexOfFirst { it.id == stop.id }
            list.getOrNull(idx - 1)
        }
        val dist = prev?.let { straightDistanceLabel(it, stop) }
        listOfNotNull(shortAddress(stop.address), dist).joinToString(" · ")
    }
    is TripPhase.Navigating -> {
        val stop = state.trip.stops.firstOrNull { it.id == phase.stopId } ?: return null
        shortAddress(stop.address)
    }
    else -> null
}
