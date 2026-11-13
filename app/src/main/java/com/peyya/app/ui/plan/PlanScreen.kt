package com.peyya.app.ui.plan

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.PlanCandidate
import com.peyya.app.domain.PlanPhase
import com.peyya.app.domain.PlanSession
import com.peyya.app.ui.components.BackHeader
import com.peyya.app.ui.components.DestinationPreviewMap
import com.peyya.app.ui.components.MiniRouteMap
import com.peyya.app.ui.components.OverlapPhotoCards
import com.peyya.app.ui.components.PlaceSearchField
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.RoutePhotoRow
import com.peyya.app.ui.components.ScreenScaffold
import com.peyya.app.ui.components.SectionTitle
import com.peyya.app.ui.components.SoftCard
import com.peyya.app.ui.theme.PeyyaBrand
import java.util.Calendar

@Composable
fun PlanScreen(
    contentPadding: PaddingValues,
    session: PlanSession,
    onChip: (String) -> Unit,
    onQuickDestination: (String) -> Unit,
    onAnswer: (String) -> Unit,
    onSelectCandidate: (String) -> Unit,
    onConfirm: (Long) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    var draft by remember(session.step, session.phase) { mutableStateOf("") }
    ScreenScaffold(contentPadding) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (session.phase) {
                PlanPhase.Collecting -> CollectingFlow(
                    session = session,
                    draft = draft,
                    onDraft = { draft = it },
                    onChip = onChip,
                    onAnswer = onAnswer,
                    onQuickDestination = onQuickDestination,
                )
                PlanPhase.Candidates -> CandidatesBlock(
                    candidates = session.candidates,
                    onSelect = onSelectCandidate,
                    onBack = onBack,
                )
                PlanPhase.Confirm -> ConfirmBlock(
                    session = session,
                    onConfirm = onConfirm,
                    onReset = onReset,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun CollectingFlow(
    session: PlanSession,
    draft: String,
    onDraft: (String) -> Unit,
    onChip: (String) -> Unit,
    onAnswer: (String) -> Unit,
    onQuickDestination: (String) -> Unit,
) {
    if (session.step == 0) {
        Text(
            "어디로 갈까요?",
            style = MaterialTheme.typography.titleLarge,
            color = PeyyaBrand,
        )
        Text(
            "목적지를 검색하거나 인기 코스를 고르세요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        PlaceSearchField(
            label = "목적지 검색",
            value = draft,
            onValueChange = onDraft,
        )
        Spacer(Modifier.height(10.dp))
        DestinationPreviewMap(query = draft)
        Spacer(Modifier.height(12.dp))
        PrimaryCta("이 목적지로 다음", draft.isNotBlank()) {
            onAnswer(draft)
            onDraft("")
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("추천")
        Spacer(Modifier.height(8.dp))
        OverlapPhotoCards(
            leftTitle = "용산 박물관",
            leftImage = PeyyaImages.MUSEUM,
            rightTitle = "성수 감성",
            rightImage = PeyyaImages.SEONGSU,
            onLeft = { onQuickDestination("용산 박물관") },
            onRight = { onQuickDestination("성수") },
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("용산 박물관", "성수", "분당 공원").forEach { chip ->
                AssistChip(onClick = { onQuickDestination(chip) }, label = { Text(chip) })
            }
        }
    } else {
        val prompt = when (session.step) {
            1 -> "누구와 함께하나요?"
            2 -> "언제쯤 움직이나요?"
            else -> "스타일은?"
        }
        SoftCard {
            Text(prompt, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "질문 ${session.step}/3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        val chips = when (session.step) {
            1 -> listOf("가족 4명", "부부", "아이 포함")
            2 -> listOf("오전", "점심", "오후")
            else -> listOf("여유롭게", "알차게", "맛집 중심")
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEach { chip ->
                AssistChip(onClick = { onChip(chip) }, label = { Text(chip) })
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = draft,
            onValueChange = onDraft,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("직접 입력") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(Modifier.height(12.dp))
        PrimaryCta("다음", draft.isNotBlank()) {
            onAnswer(draft)
            onDraft("")
        }
    }
}

@Composable
private fun CandidatesBlock(
    candidates: List<PlanCandidate>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    BackHeader(title = "추천 코스", onBack = onBack, backLabel = "처음으로")
    SectionTitle("지도로 보는 ${candidates.size}개 코스")
    Spacer(Modifier.height(8.dp))
    candidates.forEach { candidate ->
        SoftCard {
            MiniRouteMap(candidate.stops, heightDp = 200)
            Spacer(Modifier.height(10.dp))
            Text(candidate.title, style = MaterialTheme.typography.titleMedium)
            Text(
                candidate.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta("이 코스 선택", true) { onSelect(candidate.id) }
        }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun ConfirmBlock(
    session: PlanSession,
    onConfirm: (Long) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    BackHeader(title = "일정 확정", onBack = onBack, backLabel = "코스 목록")
    val selected = session.candidates.firstOrNull { it.id == session.selectedCandidateId }
    if (selected != null) {
        MiniRouteMap(selected.stops, heightDp = 200)
        Spacer(Modifier.height(10.dp))
        RoutePhotoRow(
            title = selected.title,
            imageModel = PeyyaImages.forRegion(selected.region),
            meta = "일시만 확정하면 실행 탭에 올라갑니다.",
        )
        Spacer(Modifier.height(12.dp))
    }
    PrimaryCta("오늘 오후로 확정", selected != null) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        onConfirm(cal.timeInMillis)
    }
    Spacer(Modifier.height(8.dp))
    PrimaryCta("내일 오전 10시로 확정", selected != null) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        onConfirm(cal.timeInMillis)
    }
    TextButton(onClick = onReset) { Text("다시 짜기") }
}
