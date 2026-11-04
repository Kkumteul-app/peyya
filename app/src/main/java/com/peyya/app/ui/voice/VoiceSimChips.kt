package com.peyya.app.ui.voice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.voice.VoiceSimUtterances

@Composable
fun VoiceSimChips(
    carMode: Boolean,
    onSimulate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelColor = if (carMode) CarTextSub else PeyyaInkSub
    val chipColor = if (carMode) CarBrandLight else PeyyaBrand
    val line = if (carMode) CarLineStrong else PeyyaLine

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            VoiceSimUtterances.SECTION_TITLE,
            color = labelColor,
            fontSize = if (carMode) 16.sp else 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            VoiceChip(VoiceSimUtterances.LABEL_NEXT, carMode, chipColor, line, Modifier.weight(1f)) {
                onSimulate(VoiceSimUtterances.NEXT)
            }
            VoiceChip(VoiceSimUtterances.LABEL_SKIP, carMode, chipColor, line, Modifier.weight(1f)) {
                onSimulate(VoiceSimUtterances.SKIP)
            }
            VoiceChip(VoiceSimUtterances.LABEL_KEEP, carMode, chipColor, line, Modifier.weight(1f)) {
                onSimulate(VoiceSimUtterances.KEEP)
            }
        }
    }
}

@Composable
fun EditLockHint(
    carMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (carMode) CarTextSub else PeyyaInkDim
    Text(
        VoiceUiVisibility.EDIT_LOCK_HINT,
        color = color,
        fontSize = if (carMode) 16.sp else 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier,
    )
}

@Composable
private fun VoiceChip(
    label: String,
    carMode: Boolean,
    chipColor: androidx.compose.ui.graphics.Color,
    line: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = chipColor,
        fontSize = if (carMode) 16.sp else 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = if (carMode) 12.dp else 8.dp),
    )
}
