package com.peyya.app.ui.plan

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.PlaceCategory
import kotlin.math.max
import android.graphics.Color as AndroidColor

/** 기아 커넥트 지도에 가까운 다크 톤 */
private val MapBg = Color(0xFF1A1F26)
private val MapBlock = Color(0xFF232A33)
private val RoadMajor = Color(0xFF3A4552)
private val RoadMinor = Color(0xFF2C343E)
private val RoadLabel = Color(0xFF8A93A0)
private val Water = Color(0xFF1E3A4A)
private val RouteLine = Color(0xFF2ED573)
private val StartPin = Color(0xFF2ED573)
private val EndPin = Color(0xFFFF5A5A)
private val ViaPin = Color(0xFF4C8DFF)

private val PoiFood = Color(0xFFFF8A3D)
private val PoiCafe = Color(0xFFC48BFF)
private val PoiCulture = Color(0xFF5B8CFF)
private val PoiOutdoor = Color(0xFF3DDB8A)
private val PoiDefault = Color(0xFF7A8494)

/**
 * 기아 커넥트형 다크 지도 — 도로·POI·경로 핀.
 */
@Composable
fun PlanMapCanvas(
    routeStops: List<MapPlace>,
    focus: MapPlace?,
    nearbyPois: List<MapPlace> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val routePins = when {
        routeStops.isNotEmpty() -> routeStops
        focus != null -> listOf(focus)
        else -> emptyList()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(MapBg)

        // 블록 느낌의 면
        for (i in 0..5) {
            val left = (i * 0.18f) * size.width
            val top = ((i % 3) * 0.22f) * size.height
            drawRoundRect(
                color = MapBlock,
                topLeft = Offset(left, top + 40.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(size.width * 0.16f, size.height * 0.14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            )
        }

        // 격자 도로
        val step = 42.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(RoadMinor, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx())
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(RoadMinor, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
            y += step
        }

        // 주요 도로
        drawLine(
            RoadMajor,
            Offset(0f, size.height * 0.38f),
            Offset(size.width, size.height * 0.44f),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            RoadMajor,
            Offset(size.width * 0.22f, 0f),
            Offset(size.width * 0.48f, size.height),
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            RoadMajor,
            Offset(0f, size.height * 0.72f),
            Offset(size.width, size.height * 0.66f),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // 물줄기
        val river = Path().apply {
            moveTo(0f, size.height * 0.58f)
            cubicTo(
                size.width * 0.3f, size.height * 0.52f,
                size.width * 0.6f, size.height * 0.64f,
                size.width, size.height * 0.55f,
            )
            lineTo(size.width, size.height * 0.62f)
            cubicTo(
                size.width * 0.6f, size.height * 0.72f,
                size.width * 0.3f, size.height * 0.60f,
                0f, size.height * 0.66f,
            )
            close()
        }
        drawPath(river, Water)

        val allForBounds = (routePins + nearbyPois).distinctBy { it.id }
        if (allForBounds.isEmpty()) return@Canvas

        val lats = allForBounds.map { it.latitude }
        val lngs = allForBounds.map { it.longitude }
        val minLat = lats.minOrNull()!!
        val maxLat = lats.maxOrNull()!!
        val minLng = lngs.minOrNull()!!
        val maxLng = lngs.maxOrNull()!!
        val padLat = max((maxLat - minLat) * 0.28, 0.015)
        val padLng = max((maxLng - minLng) * 0.28, 0.015)
        val lat0 = minLat - padLat
        val lat1 = maxLat + padLat
        val lng0 = minLng - padLng
        val lng1 = maxLng + padLng

        fun project(place: MapPlace): Offset {
            val nx = ((place.longitude - lng0) / (lng1 - lng0)).toFloat().coerceIn(0.08f, 0.92f)
            val ny = (1.0 - (place.latitude - lat0) / (lat1 - lat0)).toFloat().coerceIn(0.14f, 0.86f)
            return Offset(nx * size.width, ny * size.height)
        }

        // 주변 POI 아이콘 (작은 컬러 점)
        nearbyPois.forEach { place ->
            if (routePins.any { it.id == place.id }) return@forEach
            val p = project(place)
            val color = when (place.category) {
                PlaceCategory.FOOD -> PoiFood
                PlaceCategory.CAFE -> PoiCafe
                PlaceCategory.CULTURE -> PoiCulture
                PlaceCategory.OUTDOOR -> PoiOutdoor
                else -> PoiDefault
            }
            drawCircle(Color.White.copy(alpha = 0.9f), 9.dp.toPx(), p)
            drawCircle(color, 7.dp.toPx(), p)
        }

        val points = routePins.map { project(it) }
        if (points.size >= 2) {
            for (i in 0 until points.lastIndex) {
                drawLine(
                    color = RouteLine,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(14.dp.toPx()),
                )
            }
        }

        val labelPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 10.dp.toPx()
            setColor(AndroidColor.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        val nameBg = Paint().apply {
            isAntiAlias = true
            setColor(AndroidColor.argb(220, 28, 32, 40))
        }
        val namePaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 11.dp.toPx()
            setColor(AndroidColor.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        routePins.forEachIndexed { index, place ->
            val p = points[index]
            val color = when (index) {
                0 -> StartPin
                routePins.lastIndex -> EndPin
                else -> ViaPin
            }
            val label = when (index) {
                0 -> "출"
                routePins.lastIndex -> "도"
                else -> "$index"
            }
            drawCircle(Color.White, 15.dp.toPx(), p)
            drawCircle(color, 12.dp.toPx(), p)
            drawContext.canvas.nativeCanvas.drawText(label, p.x, p.y + 4.dp.toPx(), labelPaint)

            val name = place.name.take(7)
            val tw = namePaint.measureText(name)
            val left = p.x - tw / 2 - 8.dp.toPx()
            val top = p.y + 16.dp.toPx()
            drawContext.canvas.nativeCanvas.drawRoundRect(
                left, top, left + tw + 16.dp.toPx(), top + 20.dp.toPx(), 10f, 10f, nameBg,
            )
            drawContext.canvas.nativeCanvas.drawText(name, p.x, top + 14.dp.toPx(), namePaint)
        }

        // 도로명 느낌 라벨
        drawContext.canvas.nativeCanvas.drawText(
            "한강대로",
            size.width * 0.62f,
            size.height * 0.40f,
            Paint().apply {
                isAntiAlias = true
                textSize = 11.dp.toPx()
                setColor(AndroidColor.argb(160, 170, 180, 190))
            },
        )
    }
}
