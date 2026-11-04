package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarPanel
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaDanger
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine

private data class SnoozeOption(
    val label: String,
    val meta: String,
    val choice: SnoozePick,
)

private sealed class SnoozePick {
    data class Fixed(val minutes: Int) : SnoozePick()
    data object Custom : SnoozePick()
    data object Reservation : SnoozePick()
}

private val snoozeOptions = listOf(
    SnoozeOption("30분 후", "고정", SnoozePick.Fixed(30)),
    SnoozeOption("60분 후", "고정", SnoozePick.Fixed(60)),
    SnoozeOption("90분 후", "고정", SnoozePick.Fixed(90)),
    SnoozeOption("직접 입력", "분", SnoozePick.Custom),
    SnoozeOption("예약 시간 알림", "시각", SnoozePick.Reservation),
)

/**
 * 스누즈 5선택. Phone은 하단 시트, Car는 우측 패널 전면.
 * 선택은 [onSnooze] → ViewModel.snooze / snoozeReservation.
 */
@Composable
fun SnoozePicker(
    onSnooze: (SnoozeChoice) -> Unit,
    onReservation: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    carMode: Boolean = false,
) {
    var showCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("45") }

    fun handle(pick: SnoozePick) {
        when (pick) {
            is SnoozePick.Fixed -> {
                onSnooze(SnoozeChoice.Minutes(pick.minutes))
                onDismiss()
            }
            SnoozePick.Custom -> showCustom = true
            SnoozePick.Reservation -> {
                onReservation()
                onDismiss()
            }
        }
    }

    if (carMode) {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                "스누즈",
                color = CarBrandLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "언제 다시 알려드릴까요?",
                color = CarText,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 46.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "스누즈 중 시동 → 리마인드 없음",
                color = CarTextSub,
                fontSize = 22.sp,
            )
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                snoozeOptions.forEach { option ->
                    CarSnoozeRow(
                        label = option.label,
                        meta = option.meta,
                        onClick = { handle(option.choice) },
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PeyyaInk.copy(alpha = 0.42f))
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(PeyyaCanvas)
                    .clickable(enabled = false, onClick = {})
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PeyyaLine),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "언제 다시 알려드릴까요?",
                    color = PeyyaInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "선택한 동안에는 시동이 켜져도 리마인드를 띄우지 않아요.",
                    color = PeyyaInkDim,
                    fontSize = 14.sp,
                    lineHeight = (14 * 1.45).sp,
                )
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    snoozeOptions.forEach { option ->
                        PhoneSnoozeRow(
                            label = option.label,
                            meta = option.meta,
                            onClick = { handle(option.choice) },
                        )
                    }
                }
            }
        }
    }

    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = {
                Text(
                    "몇 분 뒤에 알려드릴까요?",
                    fontSize = if (carMode) 20.sp else 18.sp,
                )
            },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { value ->
                        customText = value.filter { it.isDigit() }.take(3)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("분") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = customText.toIntOrNull()
                        if (minutes != null && minutes > 0) {
                            onSnooze(SnoozeChoice.Minutes(minutes))
                            showCustom = false
                            onDismiss()
                        }
                    },
                ) {
                    Text("확인", color = if (carMode) CarBrandLight else PeyyaInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustom = false }) {
                    Text("취소", color = PeyyaDanger)
                }
            },
            containerColor = if (carMode) CarPanel else PeyyaCanvas,
        )
    }
}

@Composable
private fun PhoneSnoozeRow(
    label: String,
    meta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, PeyyaLine, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = PeyyaInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(meta, color = PeyyaInkDim, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CarSnoozeRow(
    label: String,
    meta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .border(1.dp, CarLineStrong, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = CarText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(meta, color = CarDim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
