package com.peyya.app.ui.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.data.DestinationTipsCatalog
import com.peyya.app.domain.Stop
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft

/**
 * 내비로 가는 길에 미리 보는 목적지 정보 (볼거리 · 주차 · 가족).
 */
@Composable
fun DestinationTipsCard(
    stop: Stop,
    carMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val tips = DestinationTipsCatalog.resolve(stop)
    if (!tips.isNotEmpty()) return

    val shape = RoundedCornerShape(if (carMode) 20.dp else 16.dp)
    val bg = if (carMode) CarCard else PeyyaSoft
    val titleColor = if (carMode) CarText else PeyyaInk
    val bodyColor = if (carMode) CarTextSub else PeyyaInkDim
    val line = if (carMode) CarLineStrong else PeyyaLine
    val accent = PeyyaBrand

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, line, shape)
            .background(bg)
            .padding(if (carMode) 20.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (carMode) 14.dp else 10.dp),
    ) {
        Text(
            "${stop.name} · 가는 길에 미리 보기",
            color = titleColor,
            fontSize = if (carMode) 20.sp else 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "길안내는 차량 내비가 맡고, 여기선 도착 전 팁만 보여줘요.",
            color = bodyColor,
            fontSize = if (carMode) 16.sp else 12.sp,
        )
        if (tips.accessFacilities.isNotEmpty()) {
            Text(
                "편의 시설",
                color = accent,
                fontSize = if (carMode) 16.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tips.accessFacilities.forEach { facility ->
                    Text(
                        facility,
                        color = if (carMode) CarText else PeyyaInk,
                        fontSize = if (carMode) 15.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (carMode) CarLineStrong.copy(alpha = 0.45f) else PeyyaBrand.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
        TipBlock("볼거리 · 봐야 할 것", tips.highlights, titleColor, bodyColor, accent, carMode)
        TipBlock("주차 팁", tips.parkingTips, titleColor, bodyColor, accent, carMode)
        TipBlock("가족 동반 · 유모차·휠체어", tips.familyTips, titleColor, bodyColor, accent, carMode)
    }
}

@Composable
private fun TipBlock(
    title: String,
    items: List<String>,
    titleColor: androidx.compose.ui.graphics.Color,
    bodyColor: androidx.compose.ui.graphics.Color,
    accent: androidx.compose.ui.graphics.Color,
    carMode: Boolean,
) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(if (carMode) 8.dp else 4.dp)) {
        Text(
            title,
            color = accent,
            fontSize = if (carMode) 16.sp else 12.sp,
            fontWeight = FontWeight.Bold,
        )
        items.forEach { item ->
            Text(
                "· $item",
                color = bodyColor,
                fontSize = if (carMode) 18.sp else 13.sp,
            )
        }
    }
}
