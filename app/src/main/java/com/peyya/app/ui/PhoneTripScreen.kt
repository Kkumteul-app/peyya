package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.data.TripThemeAnalyzer
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.labelKo
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.ui.adapt.AdaptSuggestionPanel
import com.peyya.app.ui.adapt.AdaptUiVisibility
import com.peyya.app.ui.booking.BookingPrepSection
import com.peyya.app.ui.booking.BookingUiVisibility
import com.peyya.app.ui.tips.DestinationTipsCard
import com.peyya.app.ui.components.ArrivalConfirmBanner
import com.peyya.app.ui.components.DemoBadge
import com.peyya.app.ui.components.IgnitionReminderActions
import com.peyya.app.ui.components.IgnitionReminderCard
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.components.SnoozePicker
import com.peyya.app.ui.components.StopSelectRow
import com.peyya.app.ui.components.StopState
import com.peyya.app.ui.components.StopTimelineRow
import com.peyya.app.ui.review.PhoneReviewOrCourseOverlay
import com.peyya.app.ui.review.ReviewFlowCallbacks
import com.peyya.app.ui.review.showsReviewOrCourseUi
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaDanger
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaSpace
import com.peyya.app.ui.voice.EditLockHint
import com.peyya.app.ui.voice.VoiceSimChips
import com.peyya.app.ui.voice.VoiceUiVisibility
import com.peyya.app.viewmodel.TripUiState

@Composable
fun PhoneTripScreen(
    state: TripUiState,
    onToggleStop: (String, Boolean) -> Unit,
    onStartRoute: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onReplayGuideSpeech: () -> Unit = {},
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
    onSnooze: (SnoozeChoice) -> Unit = {},
    onSnoozeReservation: (Long) -> Unit = {},
    onSkipToday: () -> Unit = {},
    onConfirmArrivalFromPrompt: () -> Unit = {},
    onDismissArrivalConfirm: () -> Unit = {},
    reviewCallbacks: ReviewFlowCallbacks = ReviewFlowCallbacks(),
    onAcceptAdapt: (String) -> Unit = {},
    onDismissAdapt: () -> Unit = {},
    onSimulateVoice: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    val actions = resolveTripActions(
        state, onStartRoute, onMarkArrived, onIgnition, onConfirmNext, onSkipNext, onResetTrip,
    )
    val (title, subtitle) = heroCopy(state)
    val selected = state.trip.selectedStops()
    val done = doneCount(state)
    val showVoice = state.lastSpoken.isNotBlank() &&
        (state.phase is TripPhase.Navigating ||
            state.phase is TripPhase.AwaitingNext ||
            state.phase is TripPhase.Arrived)
    val nextPreview = nextStopAfterArrived(state)
    val arrivedStop = currentStop(state)
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
        is TripPhase.Arrived -> nextPreview?.id
        else -> null
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var showSnooze by remember { mutableStateOf(false) }
    LaunchedEffect(state.ignitionPromptVisible) {
        if (!state.ignitionPromptVisible) showSnooze = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PeyyaCanvas)
                .padding(
                    start = PeyyaSpace.screenGutter,
                    end = PeyyaSpace.screenGutter,
                    top = PeyyaSpace.sm,
                    bottom = PeyyaSpace.xs,
                ),
        ) {
            PhoneTopBar(
                mockMode = state.mockMode,
                menuExpanded = menuExpanded,
                onMenuExpand = { menuExpanded = true },
                onMenuDismiss = { menuExpanded = false },
                showMarkArrived = state.phase is TripPhase.Navigating,
                onReset = {
                    menuExpanded = false
                    showReset = true
                },
                onMarkArrived = {
                    menuExpanded = false
                    onMarkArrived()
                },
            )

            Spacer(Modifier.height(PeyyaSpace.md))

            if (state.showsReviewOrCourseUi()) {
                PhoneReviewOrCourseOverlay(
                    state = state,
                    callbacks = reviewCallbacks,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (state.ignitionPromptVisible) {
                        IgnitionReminderCard(carMode = false)
                    } else {
                        NowCard(
                            phaseLabel = "${state.phase.labelKo()} · $done / ${selected.size}",
                            etaLabel = nowCardEtaLabel(state),
                            title = title,
                            subtitle = subtitle,
                            nextName = nextPreview?.name,
                            nextMeta = nextPreview?.let { next ->
                                val from = arrivedStop
                                val dist = if (from != null) straightDistanceLabel(from, next) else null
                                listOfNotNull(shortAddress(next.address), dist).joinToString(" · ")
                            },
                            spoken = if (showVoice) state.lastSpoken else null,
                            onReplay = onReplayGuideSpeech,
                        )
                    }

                    val notice = state.notice
                    if (notice != null && !state.arrivalConfirmVisible) {
                        Spacer(Modifier.height(16.dp))
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

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "오늘의 계획",
                        color = PeyyaInkDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.trip.title.ifBlank {
                            TripThemeAnalyzer.themeSentence(selected, state.planMap.companion)
                        },
                        color = PeyyaInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    if (state.phase is TripPhase.Idle) {
                        state.trip.stops.forEach { stop ->
                            StopSelectRow(
                                stop = stop,
                                enabled = true,
                                onToggle = onToggleStop,
                            )
                        }
                    } else {
                        if (VoiceUiVisibility.showsEditLockHint(state.phase)) {
                            EditLockHint(carMode = false)
                            Spacer(Modifier.height(8.dp))
                        }
                        selected.forEachIndexed { index, stop ->
                            val stopState = when {
                                stop.id in doneIds -> StopState.Done
                                stop.id == focusId -> StopState.Current
                                else -> StopState.Upcoming
                            }
                            val short = shortAddress(stop.address)
                            val caption = when (stopState) {
                                StopState.Done -> "$short · 다녀왔어요"
                                StopState.Current -> when (state.phase) {
                                    is TripPhase.Navigating -> "$short · 지금 가는 곳"
                                    else -> "$short · 다음 장소"
                                }
                                StopState.Upcoming -> short
                            }
                            StopTimelineRow(
                                order = index + 1,
                                name = stop.name,
                                caption = caption,
                                state = stopState,
                                isLast = index == selected.lastIndex,
                                etaLabel = TripThemeAnalyzer.etaIntoStop(selected, index)?.combined,
                            )
                        }
                    }

                    // 안내 중·다음 제안: 가는 길에 볼거리·주차 팁을 미리 확인
                    val tipStop = when (val p = state.phase) {
                        is TripPhase.Navigating -> state.trip.stops.firstOrNull { it.id == p.stopId }
                        is TripPhase.AwaitingNext -> state.trip.stops.firstOrNull { it.id == p.nextStopId }
                        is TripPhase.Arrived -> nextPreview
                        else -> null
                    }
                    if (tipStop != null) {
                        Spacer(Modifier.height(16.dp))
                        DestinationTipsCard(stop = tipStop, carMode = false)
                    }

                    val bookingStops = BookingUiVisibility.visibleBookingStops(
                        state.phase,
                        state.trip,
                    )
                    if (bookingStops.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        BookingPrepSection(stops = bookingStops, carMode = false)
                    }

                    val adapt = state.adaptSuggestion
                    if (AdaptUiVisibility.showsAdaptPanel(state.phase, adapt) && adapt != null) {
                        Spacer(Modifier.height(16.dp))
                        AdaptSuggestionPanel(
                            suggestion = adapt,
                            carMode = false,
                            onAccept = onAcceptAdapt,
                            onDismiss = onDismissAdapt,
                        )
                    }

                    if (VoiceUiVisibility.showsVoiceSimChips(state.phase, state.mockMode)) {
                        Spacer(Modifier.height(16.dp))
                        VoiceSimChips(carMode = false, onSimulate = onSimulateVoice)
                    }
                }

                PhoneActionDock {
                    if (state.ignitionPromptVisible) {
                        IgnitionReminderActions(
                            onStartDriving = onStartRoute,
                            onSnoozeLater = { showSnooze = true },
                            onSkipToday = onSkipToday,
                            startEnabled = selected.isNotEmpty(),
                            carMode = false,
                        )
                    } else {
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
                            SecondaryCta(actions.secondaryLabel, actions.onSecondary)
                        }
                        if (primaryLabel == null &&
                            actions.secondaryLabel == null &&
                            state.phase is TripPhase.Arrived
                        ) {
                            Text(
                                "다음 제안 대기 중 · 지금은 눌러야 할 버튼이 없어요",
                                color = PeyyaInkDim,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        if (showSnooze) {
            SnoozePicker(
                onSnooze = onSnooze,
                onReservation = {
                    onSnoozeReservation(System.currentTimeMillis() + 2 * 60 * 60_000L)
                },
                onDismiss = { showSnooze = false },
                carMode = false,
            )
        }

        if (state.arrivalConfirmVisible) {
            ArrivalConfirmBanner(
                onConfirmArrival = onConfirmArrivalFromPrompt,
                onDismiss = onDismissArrivalConfirm,
                carMode = false,
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
private fun PhoneTopBar(
    mockMode: Boolean,
    menuExpanded: Boolean,
    onMenuExpand: () -> Unit,
    onMenuDismiss: () -> Unit,
    showMarkArrived: Boolean,
    onReset: () -> Unit,
    onMarkArrived: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "페이야",
            color = PeyyaInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.3).sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DemoBadge(visible = mockMode, onDark = false)
            Box {
                IconButton(onClick = onMenuExpand, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "더보기",
                        tint = PeyyaInk,
                        modifier = Modifier.size(22.dp),
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = onMenuDismiss) {
                    DropdownMenuItem(
                        text = { Text("일정 처음부터 다시") },
                        onClick = onReset,
                    )
                    if (showMarkArrived) {
                        DropdownMenuItem(
                            text = { Text("도착으로 표시") },
                            onClick = onMarkArrived,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowCard(
    phaseLabel: String,
    etaLabel: String? = null,
    title: String,
    subtitle: String,
    nextName: String?,
    nextMeta: String?,
    spoken: String?,
    onReplay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PeyyaBrand)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                phaseLabel,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            if (!etaLabel.isNullOrBlank()) {
                Text(
                    etaLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Column {
            Text(
                title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                letterSpacing = (-0.4).sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp,
            )
        }
        if (nextName != null && nextMeta != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "다음",
                    color = PeyyaInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(nextName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(nextMeta, color = Color.White, fontSize = 13.sp)
                }
            }
        }
        if (spoken != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    spoken,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onReplay, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Rounded.Replay,
                        contentDescription = "다시 듣기",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
