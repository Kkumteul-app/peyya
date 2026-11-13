package com.peyya.app.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteMetrics(
    val distanceKm: Double,
    val etaMinutes: Int,
    val estimated: Boolean = true,
)

data class LeaveHomeWindow(
    val leaveAtEpochMs: Long,
    val arriveHomeEpochMs: Long,
    val driveMinutes: Int,
    val visitMinutes: Int,
)

object RouteMetricsEstimator {
    /** Haversine sum + ~35km/h urban average. Marked estimated for contest. */
    fun estimate(stops: List<Stop>, avgKmh: Double = 35.0): RouteMetrics? {
        if (stops.isEmpty()) return null
        if (stops.size == 1) {
            return RouteMetrics(distanceKm = 3.0, etaMinutes = 12)
        }
        var meters = 0.0
        for (i in 0 until stops.lastIndex) {
            meters += haversineM(
                stops[i].latitude, stops[i].longitude,
                stops[i + 1].latitude, stops[i + 1].longitude,
            )
        }
        // return home (approx reverse to first)
        meters += haversineM(
            stops.last().latitude, stops.last().longitude,
            stops.first().latitude, stops.first().longitude,
        )
        val km = meters / 1000.0
        val minutes = ((km / avgKmh) * 60.0).roundToInt().coerceAtLeast(1)
        return RouteMetrics(distanceKm = (km * 10).roundToInt() / 10.0, etaMinutes = minutes)
    }

    /** Leave now (rounded) → arrive back home after drive + stop stays. */
    fun leaveHomeWindow(stops: List<Stop>, nowEpochMs: Long = System.currentTimeMillis()): LeaveHomeWindow? {
        val metrics = estimate(stops) ?: return null
        val visit = (stops.size * 35).coerceAtLeast(30)
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = nowEpochMs
            val m = get(java.util.Calendar.MINUTE)
            val bump = ((m + 4) / 5) * 5
            if (bump >= 60) {
                add(java.util.Calendar.HOUR_OF_DAY, 1)
                set(java.util.Calendar.MINUTE, 0)
            } else {
                set(java.util.Calendar.MINUTE, bump)
            }
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val leave = cal.timeInMillis
        val arrive = leave + (metrics.etaMinutes + visit) * 60_000L
        return LeaveHomeWindow(
            leaveAtEpochMs = leave,
            arriveHomeEpochMs = arrive,
            driveMinutes = metrics.etaMinutes,
            visitMinutes = visit,
        )
    }

    private fun haversineM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}

object ExecuteCopyPool {
    fun lines(nickname: String): List<String> {
        val n = nickname.ifBlank { "우리 차" }
        return listOf(
            "${n}과 함께 드라이브 코스를 짜보세요.",
            "오늘은 ${n}으로 어디로 떠나볼까요?",
            "${n} 시동 걸기 전에, 다음 장소만 정해 두면 돼요.",
            "페이야가 ${n}의 주말 동선을 이어 줄게요.",
            "대기 중인 계획이 없어요. ${n}과 새 코스를 만들어 보세요.",
        )
    }

    fun pick(nickname: String, seed: Int = (System.currentTimeMillis() / 60_000).toInt()): String {
        val pool = lines(nickname)
        val idx = seed.mod(pool.size).let { if (it < 0) it + pool.size else it }
        return pool[idx]
    }
}
