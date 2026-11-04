package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft

/**
 * 시동 리마인드 3선택. [visible]일 때만 카드·CTA를 그린다.
 * Phone: quiet-card + soft/text CTA. Car: 우측 패널 히어로 + ≥64dp 타깃.
 */
@Composable
fun IgnitionReminderCard(
    modifier: Modifier = Modifier,
    carMode: Boolean = false,
) {
    if (carMode) {
        Column(modifier = modifier) {
            Text(
                "시동 감지",
                color = CarBrandLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "오늘 루트를\n시작할까요?",
                color = CarText,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 56.sp,
                letterSpacing = (-0.6).sp,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "시동만으로 안내가 시작되지 않아요.\n「운전 시작」을 선택해 주세요.",
                color = CarTextSub,
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, PeyyaLine, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PeyyaCanvas)
                .padding(16.dp),
        ) {
            Text(
                "시동 감지 · Idle",
                color = PeyyaBrand,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "오늘 루트를\n시작할까요?",
                color = PeyyaInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = (22 * 1.3).sp,
                letterSpacing = (-0.02).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "시동만으로는 안내가 시작되지 않아요. 「운전 시작」을 눌러 주세요.",
                color = PeyyaInkDim,
                fontSize = 14.sp,
                lineHeight = (14 * 1.45).sp,
            )
        }
    }
}

@Composable
fun IgnitionReminderActions(
    onStartDriving: () -> Unit,
    onSnoozeLater: () -> Unit,
    onSkipToday: () -> Unit,
    startEnabled: Boolean,
    modifier: Modifier = Modifier,
    carMode: Boolean = false,
) {
    if (carMode) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryCta(
                label = "운전 시작",
                enabled = startEnabled,
                onClick = onStartDriving,
                height = 84.dp,
                corner = 20.dp,
                textSize = 26.sp,
                textWeight = FontWeight.ExtraBold,
            )
            CarOutlinedCta(
                label = "나중에 확인",
                onClick = onSnoozeLater,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(1.dp, CarLineStrong, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onSkipToday),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "오늘 이용하지 않음",
                    color = CarTextSub,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            PrimaryCta(
                label = "운전 시작",
                enabled = startEnabled,
                onClick = onStartDriving,
            )
            Spacer(Modifier.height(8.dp))
            SoftCta(label = "나중에 확인", onClick = onSnoozeLater)
            Spacer(Modifier.height(4.dp))
            DimTextCta(label = "오늘 이용하지 않음", onClick = onSkipToday)
        }
    }
}

@Composable
fun SoftCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PeyyaSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = PeyyaInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun DimTextCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = PeyyaInkDim,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
