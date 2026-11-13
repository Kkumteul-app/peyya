package com.peyya.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.RouteMetricsEstimator
import com.peyya.app.domain.Stop
import com.peyya.app.ui.theme.PeyyaBrand

@Composable
fun MiniRouteMap(
    stops: List<Stop>,
    modifier: Modifier = Modifier,
    heightDp: Int = 200,
) {
    if (stops.isEmpty()) return
    val metrics = RouteMetricsEstimator.estimate(stops)
    val shape = RoundedCornerShape(18.dp)
    Column(modifier.fillMaxWidth()) {
        Text("코스 지도", style = MaterialTheme.typography.labelLarge, color = PeyyaBrand)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(shape)
                .background(Color(0xFFE8EEF9))
                .border(1.5.dp, PeyyaBrand.copy(alpha = 0.35f), shape),
        ) {
            // grid feel
            Canvas(Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(Color.White.copy(0.55f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(Color.White.copy(0.55f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    y += step
                }
            }
            Canvas(Modifier.fillMaxSize().padding(20.dp)) {
                if (stops.size == 1) {
                    drawCircle(PeyyaBrand, radius = 12.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                    return@Canvas
                }
                val lats = stops.map { it.latitude }
                val lons = stops.map { it.longitude }
                val minLat = lats.minOrNull() ?: return@Canvas
                val maxLat = lats.maxOrNull() ?: return@Canvas
                val minLon = lons.minOrNull() ?: return@Canvas
                val maxLon = lons.maxOrNull() ?: return@Canvas
                val dLat = (maxLat - minLat).coerceAtLeast(0.004)
                val dLon = (maxLon - minLon).coerceAtLeast(0.004)
                fun pt(lat: Double, lon: Double): Offset {
                    val x = ((lon - minLon) / dLon * size.width).toFloat()
                    val y = ((maxLat - lat) / dLat * size.height).toFloat()
                    return Offset(x.coerceIn(10f, size.width - 10f), y.coerceIn(10f, size.height - 10f))
                }
                val points = stops.map { pt(it.latitude, it.longitude) }
                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = PeyyaBrand,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)),
                    )
                }
                points.forEachIndexed { index, p ->
                    drawCircle(Color.White, radius = 14.dp.toPx(), center = p)
                    drawCircle(
                        if (index == 0) PeyyaBrand else Color(0xFF111827),
                        radius = 9.dp.toPx(),
                        center = p,
                    )
                }
            }
            Text(
                "시연용 경로 미리보기",
                style = MaterialTheme.typography.labelSmall,
                color = PeyyaBrand,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        stops.forEachIndexed { i, stop ->
            Text(
                "${i + 1}. ${stop.name}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        metrics?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                "약 ${it.distanceKm}km · ${it.etaMinutes}분 (예상)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
