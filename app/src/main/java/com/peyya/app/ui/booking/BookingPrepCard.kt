package com.peyya.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.Stop
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft

@Composable
fun BookingPrepCard(
    stop: Stop,
    carMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val url = stop.bookingUrl
    val shape = RoundedCornerShape(if (carMode) 20.dp else 16.dp)
    val bg = if (carMode) CarCard else PeyyaSoft
    val titleColor = if (carMode) CarText else PeyyaInk
    val bodyColor = if (carMode) CarTextSub else PeyyaInkDim
    val line = if (carMode) CarLineStrong else PeyyaLine

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, line, shape)
            .background(bg)
            .padding(if (carMode) 20.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (carMode) 12.dp else 8.dp),
    ) {
        Text(
            "${stop.name} · 출발 전 준비",
            color = titleColor,
            fontSize = if (carMode) 20.sp else 15.sp,
            fontWeight = FontWeight.Bold,
        )
        stop.prepChecklist.forEach { item ->
            Text(
                "· $item",
                color = bodyColor,
                fontSize = if (carMode) 18.sp else 13.sp,
            )
        }
        Text(
            BookingUiVisibility.NO_IN_APP_PAYMENT,
            color = bodyColor,
            fontSize = if (carMode) 16.sp else 12.sp,
            fontWeight = FontWeight.Medium,
        )
        if (BookingUiVisibility.canOpenBookingUrl(stop) && url != null) {
            Spacer(Modifier.height(2.dp))
            TextButton(
                onClick = {
                    runCatching { uriHandler.openUri(url) }
                },
            ) {
                Text(
                    "외부 예매 열기",
                    color = PeyyaBrand,
                    fontSize = if (carMode) 18.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun BookingPrepSection(
    stops: List<Stop>,
    carMode: Boolean,
    modifier: Modifier = Modifier,
) {
    if (stops.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (carMode) 16.dp else 12.dp),
    ) {
        stops.forEach { stop ->
            BookingPrepCard(stop = stop, carMode = carMode)
        }
    }
}
