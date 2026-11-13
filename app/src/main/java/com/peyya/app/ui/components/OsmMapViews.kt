package com.peyya.app.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.Stop
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OsmWebMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Int = 14,
    markers: List<Pair<Double, Double>> = listOf(latitude to longitude),
) {
    val html = remember(latitude, longitude, zoom, markers) {
        buildLeafletHtml(latitude, longitude, zoom, markers)
    }
    Box(modifier.clip(RoundedCornerShape(16.dp))) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    setBackgroundColor(0x00000000)
                    loadDataWithBaseURL(
                        "https://unpkg.com/",
                        html,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }
            },
            update = { it.loadDataWithBaseURL("https://unpkg.com/", html, "text/html", "UTF-8", null) },
        )
    }
}

@Composable
fun DestinationPreviewMap(
    query: String,
    modifier: Modifier = Modifier,
    heightDp: Int = 168,
) {
    val place = com.peyya.app.data.PlaceSearchCatalog.resolve(query)
        ?: com.peyya.app.data.PlaceSearchCatalog.defaultCenter
    val pinned = com.peyya.app.data.PlaceSearchCatalog.resolve(query) != null
    Column(modifier.fillMaxWidth()) {
        OsmWebMap(
            latitude = place.latitude,
            longitude = place.longitude,
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .border(1.dp, Color(0xFF2F6BFF).copy(0.25f), RoundedCornerShape(16.dp)),
            zoom = if (pinned) 15 else 12,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (pinned) "${place.detail} · ${place.label}" else "지도에서 위치를 확인하세요",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Course board inspired by trip photo-route maps:
 * real OSM base + numbered circular stop photos + connecting path.
 */
@Composable
fun CourseRouteBoard(
    title: String,
    subtitle: String,
    stops: List<Stop>,
    modifier: Modifier = Modifier,
    heightDp: Int = 260,
) {
    if (stops.isEmpty()) return
    val centerLat = stops.map { it.latitude }.average()
    val centerLon = stops.map { it.longitude }.average()
    val markers = stops.map { it.latitude to it.longitude }
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))) {
            Surface(color = Color(0xFFFF8A3D), modifier = Modifier.fillMaxWidth()) {
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1A1A1A),
                )
            }
            Surface(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth()) {
                Text(
                    subtitle,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(Color(0xFFB8D4EA)),
        ) {
            OsmWebMap(
                latitude = centerLat,
                longitude = centerLon,
                modifier = Modifier.fillMaxSize(),
                zoom = if (stops.size <= 2) 13 else 12,
                markers = markers,
            )
            // Photo pin overlay (like reference circular photos)
            val dens = LocalDensity.current
            val wPx = with(dens) { maxWidth.toPx() }
            val hPx = with(dens) { maxHeight.toPx() }
            val points = remember(stops, wPx, hPx) { projectStops(stops, wPx, hPx) }
            Canvas(Modifier.fillMaxSize()) {
                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = Color(0xFFB91C1C),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            points.forEachIndexed { index, p ->
                val stop = stops[index]
                val img = PeyyaImages.forStopId(stop.id)
                Box(
                    Modifier
                        .offset { IntOffset((p.x - 22.dp.toPx()).roundToInt(), (p.y - 22.dp.toPx()).roundToInt()) }
                        .size(44.dp),
                ) {
                    AssetPhoto(
                        img,
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        stop.name,
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopStart)
                            .offset((-2).dp, (-2).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

private fun projectStops(stops: List<Stop>, width: Float, height: Float): List<Offset> {
    val lats = stops.map { it.latitude }
    val lons = stops.map { it.longitude }
    val minLat = lats.minOrNull() ?: return emptyList()
    val maxLat = lats.maxOrNull() ?: return emptyList()
    val minLon = lons.minOrNull() ?: return emptyList()
    val maxLon = lons.maxOrNull() ?: return emptyList()
    val dLat = (maxLat - minLat).coerceAtLeast(0.008)
    val dLon = (maxLon - minLon).coerceAtLeast(0.008)
    val pad = 0.14f
    return stops.map { s ->
        val x = (((s.longitude - minLon) / dLon).toFloat() * (1 - 2 * pad) + pad) * width
        val y = (((maxLat - s.latitude) / dLat).toFloat() * (1 - 2 * pad) + pad) * height
        Offset(x, y)
    }
}

private fun buildLeafletHtml(
    lat: Double,
    lon: Double,
    zoom: Int,
    markers: List<Pair<Double, Double>>,
): String {
    val markerJs = markers.joinToString("\n") { (mLat, mLon) ->
        "L.marker([$mLat,$mLon]).addTo(map);"
    }
    val lineJs = if (markers.size >= 2) {
        val pts = markers.joinToString(",") { "[${it.first},${it.second}]" }
        "L.polyline([$pts],{color:'#b91c1c',weight:3,opacity:0.85}).addTo(map); map.fitBounds(L.latLngBounds([$pts]).pad(0.2));"
    } else ""
    return """
        <!DOCTYPE html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>html,body,#map{margin:0;height:100%;background:#b8d4ea}</style>
        </head><body><div id="map"></div><script>
        var map=L.map('map',{zoomControl:false,attributionControl:false}).setView([$lat,$lon],$zoom);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);
        $markerJs
        $lineJs
        </script></body></html>
    """.trimIndent()
}
