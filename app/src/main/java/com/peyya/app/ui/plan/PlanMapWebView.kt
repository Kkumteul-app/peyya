package com.peyya.app.ui.plan

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.peyya.app.BuildConfig
import com.peyya.app.domain.MapPlace
import org.json.JSONArray
import org.json.JSONObject

/**
 * 실제 지도 타일(브이월드 또는 OSM) + 경로/POI 마커.
 * 국토부 브이월드 키가 있으면 WMTS Base 레이어를 쓰고, 없으면 OSM(실지도)으로 표시한다.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlanMapWebView(
    routeStops: List<MapPlace>,
    focus: MapPlace?,
    nearbyPois: List<MapPlace>,
    modifier: Modifier = Modifier,
) {
    val webView = remember { mutableListOf<WebView>() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0xFF1A1F26.toInt())
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        pushMarkers(view, routeStops, focus, nearbyPois)
                    }
                }
                loadDataWithBaseURL(
                    "https://localhost/",
                    mapHtml(BuildConfig.VWORLD_API_KEY),
                    "text/html",
                    "UTF-8",
                    null,
                )
                webView.clear()
                webView.add(this)
            }
        },
        update = { view ->
            pushMarkers(view, routeStops, focus, nearbyPois)
        },
    )

    LaunchedEffect(routeStops, focus, nearbyPois) {
        webView.firstOrNull()?.let { pushMarkers(it, routeStops, focus, nearbyPois) }
    }
}

private fun pushMarkers(
    view: WebView?,
    routeStops: List<MapPlace>,
    focus: MapPlace?,
    nearbyPois: List<MapPlace>,
) {
    if (view == null) return
    val route = JSONArray()
    routeStops.forEach { route.put(placeJson(it)) }
    val pois = JSONArray()
    nearbyPois.forEach { pois.put(placeJson(it)) }
    val focusJson = focus?.let { placeJson(it) } ?: JSONObject.NULL
    val script = "window.peyyaUpdate && window.peyyaUpdate($route, $pois, $focusJson);"
    view.post { view.evaluateJavascript(script, null) }
}

private fun placeJson(place: MapPlace): JSONObject = JSONObject()
    .put("id", place.id)
    .put("name", place.name)
    .put("lat", place.latitude)
    .put("lng", place.longitude)

private fun mapHtml(vworldKey: String): String {
    val tileJs = if (vworldKey.isNotBlank()) {
        """
        L.tileLayer('https://api.vworld.kr/req/wmts/1.0.0/$vworldKey/Base/{z}/{y}/{x}.png', {
          attribution: '© 국토교통부 브이월드(VWorld)',
          maxZoom: 19
        }).addTo(map);
        """.trimIndent()
    } else {
        """
        L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
          attribution: '© Esri · VWorld 키 설정 시 국토부 지도',
          maxZoom: 16
        }).addTo(map);
        """.trimIndent()
    }

    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>
  html,body,#map{margin:0;height:100%;background:#1A1F26}
  .peyya-pin{background:transparent;border:0}
</style>
</head>
<body>
<div id="map"></div>
<script>
  var map = L.map('map', { zoomControl:false, attributionControl:true })
    .setView([37.52386, 126.98047], 13);
  $tileJs
  var routeLayer = L.layerGroup().addTo(map);
  var poiLayer = L.layerGroup().addTo(map);
  function markerIcon(color, label) {
    return L.divIcon({
      className: 'peyya-pin',
      html: '<div style="width:26px;height:26px;border-radius:50%;background:'+color+
        ';border:2px solid #fff;color:#fff;font:700 10px sans-serif;display:flex;align-items:center;justify-content:center">'+
        label+'</div>',
      iconSize:[26,26], iconAnchor:[13,13]
    });
  }
  function fitDetailed(points) {
    if (!points || !points.length) return;
    if (points.length === 1) {
      map.setView(points[0], 14);
      return;
    }
    var b = L.latLngBounds(points);
    // 전국 스팬이면 첫 점 중심으로 상세 줌
    if (b.getNorthSouth() > 1.2 || b.getEastWest() > 1.5) {
      map.setView(points[0], 13);
      return;
    }
    map.fitBounds(b, { padding:[48,48], maxZoom:13 });
  }
  window.peyyaUpdate = function(route, pois, focus) {
    routeLayer.clearLayers();
    poiLayer.clearLayers();
    var latlngs = [];
    var searchMode = (route||[]).length === 0 && (pois||[]).length > 0;
    (pois||[]).forEach(function(p){
      L.circleMarker([p.lat,p.lng], {radius:7, color:'#fff', weight:2, fillColor: searchMode ? '#2ED573' : '#FF8A3D', fillOpacity:0.95})
        .bindTooltip(p.name, {direction:'top', opacity:0.9}).addTo(poiLayer);
      if (searchMode) latlngs.push([p.lat,p.lng]);
    });
    (route||[]).forEach(function(p, i){
      var color = i===0 ? '#2ED573' : (i===route.length-1 ? '#FF5A5A' : '#4C8DFF');
      var label = i===0 ? '출' : (i===route.length-1 ? '도' : String(i));
      L.marker([p.lat,p.lng], {icon: markerIcon(color, label)})
        .bindTooltip(p.name, {permanent:true, direction:'bottom', offset:[0,8]}).addTo(routeLayer);
      latlngs.push([p.lat,p.lng]);
    });
    if (latlngs.length >= 1) {
      if ((route||[]).length >= 2) {
        L.polyline(latlngs, {color:'#2ED573', weight:5, opacity:0.95}).addTo(routeLayer);
      }
      fitDetailed(latlngs);
    } else if (focus && focus.lat) {
      map.setView([focus.lat, focus.lng], 14);
      L.marker([focus.lat, focus.lng], {icon: markerIcon('#FF5A5A','도')}).addTo(routeLayer);
    }
  };
</script>
</body>
</html>
""".trimIndent()
}
