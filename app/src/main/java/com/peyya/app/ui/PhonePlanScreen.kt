package com.peyya.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.data.MapSearchCatalog
import com.peyya.app.data.VisitFeasibilityAnalyzer
import com.peyya.app.data.VWorldPlaceSearch
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.TripPhase
import com.peyya.app.ui.media.PeyyaImage
import com.peyya.app.ui.plan.PlanMapWebView
import com.peyya.app.viewmodel.PlanCompanion
import com.peyya.app.viewmodel.PlanTiming
import com.peyya.app.viewmodel.TripUiState
import java.util.Calendar

private val MapNight = Color(0xFF1A1F26)
private val SheetBg = Color(0xFF12151A)
private val SheetCard = Color(0xFF1E242C)
private val SheetLine = Color(0xFF2C343E)
private val TextPrimary = Color(0xFFF2F4F7)
private val TextSecondary = Color(0xFFA8B0BC)
private val AccentGreen = Color(0xFF2ED573)
private val AccentBlue = Color(0xFF4C8DFF)
private val AccentRed = Color(0xFFFF5A5A)
private val ChipBg = Color(0xFF2A313A)
private val SearchBg = Color(0xFF252B33)

private enum class PlanChip(val label: String) {
    Food("맛집"),
    Sight("볼거리"),
    Recommend("추천"),
    Parking("주차장"),
}

/**
 * 계획 탭 — 실지도(브이월드/OSM) + 검색 결과 + 기아커넥트형 시트.
 */
@Composable
fun PhonePlanScreen(
    state: TripUiState,
    contentPadding: PaddingValues,
    onToggleStop: (String, Boolean) -> Unit,
    onUpdatePlanSearch: (String) -> Unit,
    onSelectPlanAnchor: (String) -> Unit,
    onTogglePlanNearby: (String, Boolean) -> Unit,
    onConfirmPlanEnd: () -> Unit = {},
    onConfirmPlanContinue: () -> Unit = {},
    onSetPlanTiming: (PlanTiming) -> Unit = {},
    onSetPlanCompanion: (PlanCompanion) -> Unit = {},
    onSetPlanDepartAt: (Long) -> Unit = {},
    onConfirmPlanPrefs: () -> Unit = {},
    onReopenPlanPrefs: () -> Unit = {},
    onReplacePlanRouteStop: (String, String) -> Unit = { _, _ -> },
    onRemovePlanRouteStop: (String) -> Unit = {},
    onMovePlanRouteStop: (String, Int) -> Unit = { _, _ -> },
    onSaveWeekendPlan: () -> Unit = {},
    onFindPlanRoute: () -> Unit = {},
    onStartRoute: () -> Unit,
    onGoExecute: () -> Unit,
) {
    val insets = phoneContentInsets(contentPadding)
    val canEdit = state.phase is TripPhase.Idle && state.review == null
    val plan = state.planMap
    val focusManager = LocalFocusManager.current
    var activeChip by remember { mutableStateOf(PlanChip.Food) }
    val displayName = state.profile.displayName.ifBlank { "가족" }.take(3)
    val nearbyPois = when (activeChip) {
        PlanChip.Food -> plan.nearbyFood
        PlanChip.Sight -> plan.nearbySights
        PlanChip.Recommend -> plan.nearbyRecommended
        PlanChip.Parking -> emptyList()
    }.ifEmpty { plan.nearbyFood + plan.nearbySights + plan.nearbyRecommended }
    // 검색어가 있으면 결과 패널을 최우선으로 보여 준다
    val showSearchPanel = plan.query.isNotBlank() && plan.pendingPlace == null

    if (!plan.prefsReady) {
        PlanPrefsSetupPage(
            modifier = Modifier
                .fillMaxSize()
                .background(MapNight)
                .padding(insets),
            companion = plan.companion,
            timing = plan.timing,
            weekendSaved = plan.weekendSaved,
            departAtEpochMs = plan.departAtEpochMs,
            familyName = state.family.familyName,
            enabled = canEdit,
            onSelectCompanion = onSetPlanCompanion,
            onSelectTiming = onSetPlanTiming,
            onSelectDepartAt = onSetPlanDepartAt,
            onConfirm = onConfirmPlanPrefs,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MapNight)
            .padding(insets),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            ConnectSearchBar(
                query = plan.query,
                enabled = canEdit,
                onQueryChange = onUpdatePlanSearch,
                onSearch = {
                    focusManager.clearFocus()
                    plan.searchResults.firstOrNull()?.let { onSelectPlanAnchor(it.id) }
                },
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlanChip.entries.forEach { chip ->
                    ConnectChip(
                        label = chip.label,
                        selected = activeChip == chip && !showSearchPanel,
                        onClick = { activeChip = chip },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (VWorldPlaceSearch.hasApiKey()) {
                    "지도·검색: 국토교통부 브이월드"
                } else {
                    "실지도 표시 중 · 브이월드 키 없으면 오프라인 검색"
                },
                color = TextSecondary,
                fontSize = 11.sp,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SheetLine, RoundedCornerShape(16.dp)),
        ) {
            PlanMapWebView(
                routeStops = if (showSearchPanel) emptyList() else plan.routeStops,
                focus = plan.focus,
                nearbyPois = if (showSearchPanel) plan.searchResults else nearbyPois,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(SheetBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SheetLine),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                when {
                    plan.pendingPlace != null -> "코스 확인"
                    showSearchPanel -> "검색 결과"
                    else -> "${displayName}님을 위한 제안"
                },
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                when {
                    plan.pendingPlace != null -> "여기서 끝낼지, 다음 장소를 더할지 고르세요"
                    showSearchPanel && plan.searching -> "국토부 공간정보 검색 중…"
                    showSearchPanel -> plan.searchSourceLabel.ifBlank { "검색 결과" }
                    plan.focus != null -> "${plan.focus!!.name} 주변 추천"
                    else -> "장소를 검색하세요"
                },
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (plan.pendingPlace == null) {
                    PlanPrefsSummaryBar(
                        summary = plan.prefsSummaryKo(state.family.familyName),
                        enabled = canEdit,
                        onEdit = onReopenPlanPrefs,
                    )
                }
                if (plan.pendingPlace != null) {
                    CourseDecisionCard(
                        placeName = plan.pendingPlace!!.name,
                        enabled = canEdit,
                        onEnd = onConfirmPlanEnd,
                        onContinue = onConfirmPlanContinue,
                    )
                }

                when {
                    showSearchPanel && plan.searching && plan.searchResults.isEmpty() -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                color = AccentGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    showSearchPanel && plan.searchResults.isEmpty() && plan.pendingPlace == null -> {
                        Text("검색 결과가 없습니다. 다른 키워드를 입력해 보세요.", color = TextSecondary, fontSize = 13.sp)
                    }
                    showSearchPanel -> {
                        plan.searchResults.forEach { place ->
                            SuggestionRow(
                                place = place,
                                actionLabel = "코스에 넣기",
                                actionAccent = AccentGreen,
                                enabled = canEdit,
                                onClick = {
                                    focusManager.clearFocus()
                                    onSelectPlanAnchor(place.id)
                                },
                            )
                        }
                    }
                    plan.focus == null && plan.pendingPlace == null -> {
                        Text("자주 가는 곳", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        MapSearchCatalog.frequentPlaces.filter { it.id != "home" }.forEach { place ->
                            SuggestionRow(
                                place = place,
                                actionLabel = "선택",
                                actionAccent = AccentBlue,
                                enabled = canEdit,
                                onClick = { onSelectPlanAnchor(place.id) },
                            )
                        }
                    }
                    else -> {
                        if (plan.routeStops.isNotEmpty() && plan.pendingPlace == null) {
                            RouteMiniStrip(
                                stops = plan.routeStops,
                                summary = plan.routeSummary,
                                visitSummary = plan.visitSummaryKo,
                                visitChecks = plan.visitChecks,
                                routeReady = plan.routeReady,
                                enabled = canEdit,
                                onRemove = { onTogglePlanNearby(it, false) },
                                onMove = onMovePlanRouteStop,
                                onFind = onFindPlanRoute,
                            )
                            val issues = plan.visitChecks.filter { it.isIssue }
                            issues.forEach { issue ->
                                VisitIssueCard(
                                    check = issue,
                                    enabled = canEdit,
                                    onReplace = { altId -> onReplacePlanRouteStop(issue.placeId, altId) },
                                    onSkip = { onRemovePlanRouteStop(issue.placeId) },
                                )
                            }
                        }
                    }
                }

                if (!showSearchPanel && (plan.focus != null || plan.pendingPlace != null)) {
                    val withKids = MapSearchCatalog.preferKidAmenities(
                        state.family,
                        companionIsFamily = plan.companion == PlanCompanion.Family,
                    )
                    if (plan.nearbyFood.isNotEmpty()) {
                        NearbySectionTitle(
                            title = "주변 맛집",
                            subtitle = if (withKids) "등록 가족·아이 동반 · 유모차·아기의자 가능 우선" else null,
                        )
                        plan.nearbyFood.forEach { place ->
                            val onRoute = plan.isOnRoute(place.id)
                            SuggestionRow(
                                place = place,
                                actionLabel = if (onRoute) "경로 삭제" else "경로 추가",
                                actionAccent = if (onRoute) AccentRed else AccentGreen,
                                enabled = canEdit,
                                onClick = { onTogglePlanNearby(place.id, !onRoute) },
                            )
                        }
                    }
                    if (plan.nearbySights.isNotEmpty()) {
                        NearbySectionTitle(title = "주변 볼거리")
                        plan.nearbySights.forEach { place ->
                            val onRoute = plan.isOnRoute(place.id)
                            SuggestionRow(
                                place = place,
                                actionLabel = if (onRoute) "경로 삭제" else "경로 추가",
                                actionAccent = if (onRoute) AccentRed else AccentGreen,
                                enabled = canEdit,
                                onClick = { onTogglePlanNearby(place.id, !onRoute) },
                            )
                        }
                    }
                    if (plan.nearbyRecommended.isNotEmpty()) {
                        NearbySectionTitle(title = "추천 장소")
                        plan.nearbyRecommended.forEach { place ->
                            val onRoute = plan.isOnRoute(place.id)
                            SuggestionRow(
                                place = place,
                                actionLabel = if (onRoute) "경로 삭제" else "경로 추가",
                                actionAccent = if (onRoute) AccentRed else AccentGreen,
                                enabled = canEdit,
                                onClick = { onTogglePlanNearby(place.id, !onRoute) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val issueCount = plan.visitChecks.count { it.isIssue }
            if (issueCount > 0 && plan.pendingPlace == null && !showSearchPanel) {
                Text(
                    "${issueCount}곳 방문 어려움 · 대체하거나 빼고 계속할 수 있어요",
                    color = AccentRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            ConnectPrimaryButton(
                label = when {
                    !canEdit -> "실행 탭에서 이어가기"
                    plan.pendingPlace != null -> "위에서 종료/추가를 선택하세요"
                    showSearchPanel -> "검색에서 장소를 고르세요"
                    plan.timing == PlanTiming.Weekend && plan.weekendSaved -> "주말 계획 저장됨"
                    plan.timing == PlanTiming.Weekend && plan.routeReady -> "주말 계획으로 저장"
                    plan.timing == PlanTiming.Weekend && plan.routeStops.size >= 2 -> "길찾기 후 저장"
                    plan.routeReady -> "이 경로로 바로 실행"
                    plan.routeStops.size >= 2 -> "길찾기"
                    else -> "도착지를 먼저 선택"
                },
                enabled = when {
                    !canEdit -> true
                    plan.pendingPlace != null -> false
                    showSearchPanel -> false
                    plan.timing == PlanTiming.Weekend && plan.weekendSaved -> false
                    plan.routeReady -> true
                    plan.routeStops.size >= 2 -> true
                    else -> false
                },
                onClick = {
                    when {
                        !canEdit -> onGoExecute()
                        plan.timing == PlanTiming.Weekend -> {
                            if (plan.routeReady) onSaveWeekendPlan() else onFindPlanRoute()
                        }
                        plan.routeReady -> {
                            onStartRoute()
                            onGoExecute()
                        }
                        else -> onFindPlanRoute()
                    }
                },
            )
        }
    }
}

@Composable
private fun PlanPrefsSummaryBar(
    summary: String,
    enabled: Boolean,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ChipBg)
            .clickable(enabled = enabled, onClick = onEdit)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = "여행 설정\n" + summary,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text("변경", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlanPrefsSetupPage(
    modifier: Modifier = Modifier,
    companion: PlanCompanion,
    timing: PlanTiming,
    weekendSaved: Boolean,
    departAtEpochMs: Long,
    familyName: String,
    enabled: Boolean,
    onSelectCompanion: (PlanCompanion) -> Unit,
    onSelectTiming: (PlanTiming) -> Unit,
    onSelectDepartAt: (Long) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("페이야", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("여행 전에 알려 주세요", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "누구와 갈지, 언제 출발할지를 먼저 정한 뒤 지도에서 경로를 구성해요.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        PlanCompanionPicker(
            companion = companion,
            familyName = familyName,
            enabled = enabled,
            onSelect = onSelectCompanion,
        )
        PlanTimingPicker(
            timing = timing,
            weekendSaved = weekendSaved,
            enabled = enabled,
            onSelect = onSelectTiming,
        )
        PlanDepartAtPicker(
            departAtEpochMs = departAtEpochMs,
            enabled = enabled,
            onSelect = onSelectDepartAt,
        )
        Spacer(Modifier.height(8.dp))
        ConnectPrimaryButton(
            label = "설정 완료 · 지도에서 경로 짜기",
            enabled = enabled,
            onClick = onConfirm,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CourseDecisionCard(
    placeName: String,
    enabled: Boolean,
    onEnd: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SheetCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "「$placeName」을(를) 코스에 넣었어요",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "여기서 여행을 끝낼까요, 아니면 다음 경로를 더 추가할까요?",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGreen)
                    .clickable(enabled = enabled, onClick = onEnd)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("여기서 종료", color = Color(0xFF0B1210), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.2f))
                    .clickable(enabled = enabled, onClick = onContinue)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("다음 경로 추가", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConnectSearchBar(
    query: String,
    enabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SearchBg)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⌕", color = TextSecondary, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = { if (enabled) onQueryChange(it) },
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(AccentGreen),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("장소, 주소 검색", color = TextSecondary, fontSize = 15.sp)
                }
                inner()
            },
        )
    }
}

@Composable
private fun ConnectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AccentGreen else ChipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF0B1210) else TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PlanCompanionPicker(
    companion: PlanCompanion,
    familyName: String,
    enabled: Boolean,
    onSelect: (PlanCompanion) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("누구와 갈까요?", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanCompanion.entries.forEach { option ->
                val selected = companion == option
                val label = if (option == PlanCompanion.Family && familyName.isNotBlank()) {
                    familyName.take(6)
                } else {
                    option.labelKo
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) AccentBlue else ChipBg)
                        .clickable(enabled = enabled) { onSelect(option) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            label,
                            color = if (selected) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            option.hintKo,
                            color = if (selected) Color.White.copy(alpha = 0.85f) else TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanTimingPicker(
    timing: PlanTiming,
    weekendSaved: Boolean,
    enabled: Boolean,
    onSelect: (PlanTiming) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("언제 갈까요?", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanTiming.entries.forEach { option ->
                val selected = timing == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) AccentGreen else ChipBg)
                        .clickable(enabled = enabled) { onSelect(option) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            option.labelKo,
                            color = if (selected) Color(0xFF0B1210) else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            option.hintKo,
                            color = if (selected) Color(0xFF0B1210).copy(alpha = 0.7f) else TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (timing == PlanTiming.Weekend && weekendSaved) {
            Text("주말 코스로 저장됨 · 순서를 바꿔도 다시 저장할 수 있어요", color = AccentBlue, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PlanDepartAtPicker(
    departAtEpochMs: Long,
    enabled: Boolean,
    onSelect: (Long) -> Unit,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("출발 일시", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChipBg)
                .clickable(enabled = enabled) {
                    val cal = Calendar.getInstance().apply { timeInMillis = departAtEpochMs }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val next = Calendar.getInstance().apply {
                                timeInMillis = departAtEpochMs
                                set(Calendar.YEAR, y)
                                set(Calendar.MONTH, m)
                                set(Calendar.DAY_OF_MONTH, d)
                            }
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    next.set(Calendar.HOUR_OF_DAY, hour)
                                    next.set(Calendar.MINUTE, minute)
                                    next.set(Calendar.SECOND, 0)
                                    next.set(Calendar.MILLISECOND, 0)
                                    onSelect(next.timeInMillis)
                                },
                                next.get(Calendar.HOUR_OF_DAY),
                                next.get(Calendar.MINUTE),
                                true,
                            ).show()
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH),
                    ).show()
                }
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text(
                VisitFeasibilityAnalyzer.formatDepartLabel(departAtEpochMs),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text("날짜·시간을 바꾸면 운영시간에 맞춰 방문 가능 여부를 다시 봐요", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun VisitIssueCard(
    check: VisitFeasibilityAnalyzer.VisitCheck,
    enabled: Boolean,
    onReplace: (String) -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SheetCard)
            .border(1.dp, AccentRed.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(check.placeName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(check.reasonKo, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        if (check.alternatives.isNotEmpty()) {
            Text("대체 장소", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                check.alternatives.forEach { alt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AccentBlue.copy(alpha = 0.2f))
                            .clickable(enabled = enabled) { onReplace(alt.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(alt.name, color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentRed.copy(alpha = 0.18f))
                    .clickable(enabled = enabled, onClick = onSkip)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("빼고 계속", color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteMiniStrip(
    stops: List<MapPlace>,
    summary: String?,
    visitSummary: String?,
    visitChecks: List<VisitFeasibilityAnalyzer.VisitCheck>,
    routeReady: Boolean,
    enabled: Boolean,
    onRemove: (String) -> Unit,
    onMove: (String, Int) -> Unit,
    onFind: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SheetCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("경로 순서 · 위↓아래로 바꿀 수 있어요", color = TextSecondary, fontSize = 11.sp)
        if (!visitSummary.isNullOrBlank()) {
            Text(
                visitSummary,
                color = if (visitChecks.any { it.isIssue }) AccentRed else AccentGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        stops.forEachIndexed { index, stop ->
            val role = when (index) {
                0 -> "출발"
                stops.lastIndex -> "도착"
                else -> "경유"
            }
            val color = when (role) {
                "출발" -> AccentGreen
                "도착" -> AccentRed
                else -> AccentBlue
            }
            val canReorder = enabled && index > 0
            val check = visitChecks.firstOrNull { it.placeId == stop.id }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(18.dp).clip(CircleShape).background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (role == "경유") "$index" else role.take(1),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$role · ${stop.name}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (check != null) {
                        Text(
                            check.badgeKo,
                            color = if (check.isIssue) AccentRed else AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
                if (canReorder) {
                    Text(
                        "▲",
                        color = if (index > 1) AccentBlue else TextSecondary.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(enabled = index > 1) { onMove(stop.id, -1) }
                            .padding(horizontal = 4.dp),
                    )
                    Text(
                        "▼",
                        color = if (index < stops.lastIndex) AccentBlue else TextSecondary.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(enabled = index < stops.lastIndex) { onMove(stop.id, 1) }
                            .padding(horizontal = 4.dp),
                    )
                }
                if (enabled && index > 0 && stops.size > 2) {
                    Text(
                        "삭제",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { onRemove(stop.id) },
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                summary ?: "순서를 정한 뒤 길찾기",
                color = if (summary != null) AccentGreen else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (routeReady) "다시 길찾기" else "길찾기",
                color = AccentGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled && stops.size >= 2, onClick = onFind)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun NearbySectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(subtitle, color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SuggestionRow(
    place: MapPlace,
    actionLabel: String,
    actionAccent: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SheetCard)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PeyyaImage(
            model = place.photoAsset,
            contentDescription = place.name,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(place.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            val meta = buildString {
                append(place.category.labelKo)
                place.distanceMeters?.let {
                    append(" · ")
                    append(if (it < 1000) "${it}m" else String.format("%.1fkm", it / 1000.0))
                }
            }
            Text(meta, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            place.familyAmenityLabel()?.let { amenity ->
                Text(amenity, color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Text(place.address, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(actionAccent.copy(alpha = 0.16f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(actionLabel, color = actionAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConnectPrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) AccentGreen else SheetLine)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (enabled) Color(0xFF0B1210) else TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
