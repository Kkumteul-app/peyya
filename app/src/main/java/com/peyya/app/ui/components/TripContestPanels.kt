package com.peyya.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.AdaptSuggestion
import com.peyya.app.domain.CourseCandidate
import com.peyya.app.domain.Stop
import com.peyya.app.domain.VoiceCommandSimulator
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.viewmodel.ReviewUiPhase
import com.peyya.app.viewmodel.StopRatingDraft

@Composable
fun BookingPrepCard(
    stop: Stop,
    checked: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (!stop.needsBooking) return
    val context = LocalContext.current
    SoftCard {
        Text("예매 · 준비", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("인앱 결제는 없습니다. 공식 페이지로 연결합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        stop.prepChecklist.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item in checked, onCheckedChange = { onToggle(item) })
                Text(item, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (!stop.bookingUrl.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(stop.bookingUrl)))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("공식 예매 열기")
            }
        }
    }
}

@Composable
fun AdaptPanel(
    suggestions: List<AdaptSuggestion>,
    dismissed: Boolean,
    onDismiss: () -> Unit,
    onAcceptParking: (String) -> Unit,
) {
    if (dismissed || suggestions.isEmpty()) return
    SoftCard {
        Text("당일 적응 · 시연용/추정", style = MaterialTheme.typography.titleMedium, color = PeyyaBrand)
        Spacer(Modifier.height(8.dp))
        suggestions.forEach { s ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = PeyyaSoft,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(s.title, style = MaterialTheme.typography.titleSmall)
                    Text(s.body, style = MaterialTheme.typography.bodyMedium)
                    if (s.altStop != null) {
                        TextButton(onClick = { onAcceptParking(s.id) }) {
                            Text("주차 안내 받기")
                        }
                    }
                }
            }
        }
        TextButton(onClick = onDismiss) { Text("닫기") }
    }
}

@Composable
fun VoiceSimChips(onChip: (String) -> Unit) {
    Column {
        Text("음성 시뮬 (STT SDK는 로드맵)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoiceCommandSimulator.demoChips.forEach { chip ->
                AssistChip(onClick = { onChip(chip) }, label = { Text(chip) })
            }
        }
    }
}

@Composable
fun ReviewPanel(
    phase: ReviewUiPhase,
    drafts: List<StopRatingDraft>,
    index: Int,
    onContinueIntro: () -> Unit,
    onUpdate: (Int, String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    if (phase == ReviewUiPhase.None || phase == ReviewUiPhase.Done) return
    SoftCard {
        if (phase == ReviewUiPhase.Intro) {
            Text("오늘 하루는 어땠나요?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("장소별로 별점과 한 줄을 남겨 주세요.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            PrimaryCta("리뷰 시작", true, onClick = onContinueIntro)
            TextButton(onClick = onSkip) { Text("건너뛰기") }
            return@SoftCard
        }
        val draft = drafts.getOrNull(index) ?: return@SoftCard
        var stars by remember(draft.stopId) { mutableFloatStateOf(draft.stars.toFloat()) }
        var comment by remember(draft.stopId) { mutableStateOf(draft.comment) }
        Text(draft.stopName, style = MaterialTheme.typography.titleMedium)
        Text("${index + 1}/${drafts.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text("별점 ${stars.toInt()}")
        Slider(
            value = stars,
            onValueChange = {
                stars = it
                onUpdate(it.toInt(), comment)
            },
            valueRange = 1f..5f,
            steps = 3,
        )
        TextField(
            value = comment,
            onValueChange = {
                comment = it
                onUpdate(stars.toInt(), it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("한 줄 소감 (선택)") },
        )
        Spacer(Modifier.height(10.dp))
        PrimaryCta(if (index >= drafts.lastIndex) "저장" else "다음 장소", true, onClick = onNext)
        TextButton(onClick = onSkip) { Text("리뷰 건너뛰기") }
    }
}

@Composable
fun CourseRecommendPanel(
    candidates: List<CourseCandidate>,
    onApply: (String) -> Unit,
) {
    if (candidates.isEmpty()) return
    SoftCard {
        Text("추천 코스", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        candidates.forEach { c ->
            Text(c.title, style = MaterialTheme.typography.titleSmall)
            Text(c.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { onApply(c.id) }) { Text("이 코스로") }
            Spacer(Modifier.height(6.dp))
        }
    }
}
