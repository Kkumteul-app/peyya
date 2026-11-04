package com.peyya.app.ui.adapt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.adapt.AdaptCandidate
import com.peyya.app.adapt.AdaptKind
import com.peyya.app.adapt.AdaptSuggestion
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft

@Composable
fun AdaptSuggestionPanel(
    suggestion: AdaptSuggestion,
    carMode: Boolean,
    onAccept: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(if (carMode) 20.dp else 16.dp)
    val bg = if (carMode) CarCard else PeyyaSoft
    val titleColor = if (carMode) CarText else PeyyaInk
    val bodyColor = if (carMode) CarTextSub else PeyyaInkDim
    val accent = if (carMode) CarBrandLight else PeyyaBrand
    val line = if (carMode) CarLineStrong else PeyyaLine
    val level = AdaptUiVisibility.congestionLabelKo(suggestion.congestion.level)
    val demo = AdaptUiVisibility.demoLabelOrNull(suggestion)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, line, shape)
            .background(bg)
            .padding(if (carMode) 20.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (carMode) 12.dp else 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "당일 적응 · 혼잡 $level",
                color = titleColor,
                fontSize = if (carMode) 20.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (demo != null) {
                Text(
                    demo,
                    color = accent,
                    fontSize = if (carMode) 16.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, accent, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        if (suggestion.keepInRevisitQueue) {
            Text(
                "본 목적지는 재방문 큐에 남겨 둡니다.",
                color = bodyColor,
                fontSize = if (carMode) 16.sp else 12.sp,
            )
        }
        suggestion.detours.forEach { candidate ->
            AdaptCandidateRow(
                candidate = candidate,
                carMode = carMode,
                onAccept = onAccept,
            )
        }
        suggestion.parking.forEach { candidate ->
            AdaptCandidateRow(
                candidate = candidate,
                carMode = carMode,
                onAccept = onAccept,
            )
        }
        TextButton(onClick = onDismiss) {
            Text(
                "거절",
                color = bodyColor,
                fontSize = if (carMode) 18.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AdaptCandidateRow(
    candidate: AdaptCandidate,
    carMode: Boolean,
    onAccept: (String) -> Unit,
) {
    val kindLabel = when (candidate.kind) {
        AdaptKind.Detour -> "우회"
        AdaptKind.Parking -> "주차"
    }
    val titleColor = if (carMode) CarText else PeyyaInk
    val bodyColor = if (carMode) CarTextSub else PeyyaInkDim
    val accent = if (carMode) CarBrandLight else PeyyaBrand
    val line = if (carMode) CarLineStrong else PeyyaLine

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, line, RoundedCornerShape(12.dp))
            .clickable { onAccept(candidate.id) }
            .padding(horizontal = 12.dp, vertical = if (carMode) 14.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "$kindLabel · ${candidate.name}",
                color = titleColor,
                fontSize = if (carMode) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            )
            if (candidate.address.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    candidate.address,
                    color = bodyColor,
                    fontSize = if (carMode) 15.sp else 12.sp,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "수락",
            color = accent,
            fontSize = if (carMode) 18.sp else 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
