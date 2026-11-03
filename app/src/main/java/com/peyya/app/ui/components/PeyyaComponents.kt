package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.Stop
import com.peyya.app.ui.theme.CarBg
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaDanger
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine

@Composable
fun FamilyAvatars(members: List<FamilyMember>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        members.forEach { member ->
            val brush = when (member.ageBand) {
                AgeBand.CHILD -> Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
                AgeBand.TEEN -> Brush.linearGradient(listOf(Color(0xFF34D399), PeyyaBrand))
                AgeBand.SENIOR -> Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)))
                AgeBand.ADULT -> Brush.linearGradient(listOf(Color(0xFF6B8CFF), PeyyaBrand))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brush),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = member.name.take(1),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun StopThumb(stopId: String, modifier: Modifier = Modifier) {
    val brush = if (stopId.contains("museum", ignoreCase = true) || stopId == "museum") {
        Brush.linearGradient(listOf(CarLineStrong, CarBg))
    } else {
        Brush.linearGradient(listOf(PeyyaDanger, PeyyaInk))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush),
    )
}

@Composable
fun StopCard(
    stop: Stop,
    index: Int,
    selected: Boolean,
    active: Boolean,
    done: Boolean,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit,
) {
    val border = when {
        active -> PeyyaBrand
        done -> PeyyaInk
        else -> MaterialTheme.colorScheme.outline
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onToggle(stop.id, !stop.selected) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StopThumb(stop.id, Modifier.size(width = 88.dp, height = 72.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stop.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stop.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            val badge = when {
                done -> "완료"
                active -> "현재 · ${index + 1}번"
                selected -> "선택됨 · ${index + 1}번"
                else -> "미선택"
            }
            Spacer(Modifier.height(4.dp))
            Text(
                badge,
                style = MaterialTheme.typography.labelMedium,
                color = if (done) PeyyaInk else PeyyaBrand,
            )
        }
    }
}

@Composable
fun PrimaryCta(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PeyyaBrand,
            contentColor = Color.White,
            disabledContainerColor = PeyyaBrand.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryCta(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = true,
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DemoBadge(visible: Boolean, onDark: Boolean = false) {
    if (!visible) return
    val fg = if (onDark) CarTextSub else PeyyaInkDim
    val line = if (onDark) CarLineStrong else PeyyaLine
    Text(
        "데모",
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .border(1.dp, line, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
