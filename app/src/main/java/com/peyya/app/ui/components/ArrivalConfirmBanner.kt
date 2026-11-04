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
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim

private const val TITLE = "도착 완료가 확인되지 않았어요"
private const val PHONE_BODY =
    "목적지 근처에 머무른 것 같아요. 도착했다면 「도착 완료」를 눌러 주세요. 아니면 닫고 안내를 이어갈 수 있어요."
private const val CAR_BODY =
    "목적지 근처에 머무른 것 같아요. 도착했다면 「도착 완료」를, 아니면 「닫기」로 안내를 이어가세요."

/**
 * GPS 미확정 알림. Phone은 중앙 다이얼로그, Car는 우측 패널 카드 + CTA.
 * 「도착 완료」→ confirmArrivalFromPrompt, 「닫기」→ dismissArrivalConfirm.
 */
@Composable
fun ArrivalConfirmBanner(
    onConfirmArrival: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    carMode: Boolean = false,
) {
    if (carMode) {
        Column(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CarLineStrong, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CarCard)
                    .padding(28.dp),
            ) {
                Text(
                    TITLE,
                    color = CarText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    CAR_BODY,
                    color = CarTextSub,
                    fontSize = 20.sp,
                    lineHeight = (20 * 1.45).sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PrimaryCta(
                    label = "도착 완료",
                    enabled = true,
                    onClick = onConfirmArrival,
                    modifier = Modifier.weight(2f),
                    height = 84.dp,
                    corner = 20.dp,
                    textSize = 26.sp,
                    textWeight = FontWeight.ExtraBold,
                )
                CarOutlinedCta(
                    label = "닫기",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PeyyaInk.copy(alpha = 0.45f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PeyyaCanvas)
                    .clickable(enabled = false, onClick = {})
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            ) {
                Text(
                    TITLE,
                    color = PeyyaInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    PHONE_BODY,
                    color = PeyyaInkDim,
                    fontSize = 14.sp,
                    lineHeight = (14 * 1.5).sp,
                )
                Spacer(Modifier.height(20.dp))
                PrimaryCta(
                    label = "도착 완료",
                    enabled = true,
                    onClick = onConfirmArrival,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "닫기",
                        color = PeyyaInkDim,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
