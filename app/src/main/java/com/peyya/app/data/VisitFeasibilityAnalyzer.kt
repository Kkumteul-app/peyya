package com.peyya.app.data

import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.OpeningHours
import com.peyya.app.domain.PlaceCategory
import com.peyya.app.domain.defaultDwellMinutes
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 출발 시각·구간 ETA·체류로 도착 시각을 잡고 운영시간과 대조한다.
 */
object VisitFeasibilityAnalyzer {

    enum class Status { Ok, Closed, TooLate, Unknown }

    data class VisitCheck(
        val placeId: String,
        val placeName: String,
        val arriveEpochMs: Long,
        val arriveLabel: String,
        val status: Status,
        val reasonKo: String,
        val badgeKo: String,
        val alternatives: List<MapPlace> = emptyList(),
    ) {
        val isIssue: Boolean get() = status == Status.Closed || status == Status.TooLate
    }

    data class Report(
        val checks: List<VisitCheck>,
        val issueCount: Int,
        val summaryKo: String,
    )

    fun evaluate(
        routeStops: List<MapPlace>,
        departAtEpochMs: Long,
        alternativePool: List<MapPlace> = emptyList(),
    ): Report {
        val visits = routeStops.filterNot { it.id == MapSearchCatalog.home.id }
            .ifEmpty { routeStops.drop(1) }
        if (visits.isEmpty()) {
            return Report(emptyList(), 0, "방문지가 없어요")
        }

        val checks = mutableListOf<VisitCheck>()
        var cursor = departAtEpochMs
        val selected = visits.map { it.toStop() }

        visits.forEachIndexed { index, place ->
            val travelMin = TripThemeAnalyzer.etaIntoStop(selected, index)?.durationMinutes
                ?: estimateTravelMinutes(place, index, selected)
            cursor += TimeUnit.MINUTES.toMillis(travelMin.toLong())
            val hours = place.resolvedHours()
            val arriveLabel = formatClock(cursor)
            val check = classify(place, cursor, arriveLabel, hours, alternativePool)
            checks += check
            if (!check.isIssue) {
                cursor += TimeUnit.MINUTES.toMillis(place.category.defaultDwellMinutes().toLong())
            }
        }

        val issues = checks.count { it.isIssue }
        val summaryKo = when {
            issues == 0 -> "출발 ${formatClock(departAtEpochMs)} 기준 · 모두 방문 가능"
            else -> "출발 ${formatClock(departAtEpochMs)} 기준 · ${issues}곳 방문 어려움 · 대체/제외 권장"
        }
        return Report(checks = checks, issueCount = issues, summaryKo = summaryKo)
    }

    private fun classify(
        place: MapPlace,
        arriveEpochMs: Long,
        arriveLabel: String,
        hours: OpeningHours,
        pool: List<MapPlace>,
    ): VisitCheck {
        val cal = Calendar.getInstance().apply { timeInMillis = arriveEpochMs }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val window = hours.weekdayMinutes[day]
        val alts = alternativesFor(place, arriveEpochMs, pool)

        if (window == null) {
            return VisitCheck(
                placeId = place.id,
                placeName = place.name,
                arriveEpochMs = arriveEpochMs,
                arriveLabel = arriveLabel,
                status = Status.Closed,
                reasonKo = "${place.name}은(는) ${dayLabel(day)} 휴무예요. ${hours.noteKo}".trim(),
                badgeKo = "$arriveLabel · 휴무",
                alternatives = alts,
            )
        }

        val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        if (minuteOfDay < window.first) {
            // 오픈 전 도착 — 대기 가능하면 Ok로 볼 수도 있으나, 시연에선 가능으로 처리
            return VisitCheck(
                placeId = place.id,
                placeName = place.name,
                arriveEpochMs = arriveEpochMs,
                arriveLabel = arriveLabel,
                status = Status.Ok,
                reasonKo = "오픈 ${OpeningHours.fmt(window.first)} 전 도착 · 입장을 잠시 기다리면 돼요",
                badgeKo = "$arriveLabel 도착 · 가능",
                alternatives = emptyList(),
            )
        }
        if (minuteOfDay >= window.second) {
            return VisitCheck(
                placeId = place.id,
                placeName = place.name,
                arriveEpochMs = arriveEpochMs,
                arriveLabel = arriveLabel,
                status = Status.TooLate,
                reasonKo = "${place.name}은(는) ${OpeningHours.fmt(window.second)}에 문을 닫아요. 도착이 늦어요.",
                badgeKo = "$arriveLabel · 마감",
                alternatives = alts,
            )
        }
        // 마감 1시간 미만이면 TooLate에 가깝게 경고
        if (window.second - minuteOfDay < 45) {
            return VisitCheck(
                placeId = place.id,
                placeName = place.name,
                arriveEpochMs = arriveEpochMs,
                arriveLabel = arriveLabel,
                status = Status.TooLate,
                reasonKo = "도착 후 체류 시간이 짧아요 · ${OpeningHours.fmt(window.second)} 마감",
                badgeKo = "$arriveLabel · 촉박",
                alternatives = alts,
            )
        }

        return VisitCheck(
            placeId = place.id,
            placeName = place.name,
            arriveEpochMs = arriveEpochMs,
            arriveLabel = arriveLabel,
            status = Status.Ok,
            reasonKo = hours.labelFor(arriveEpochMs),
            badgeKo = "$arriveLabel 도착 · 가능",
            alternatives = emptyList(),
        )
    }

    private fun alternativesFor(
        place: MapPlace,
        arriveEpochMs: Long,
        pool: List<MapPlace>,
    ): List<MapPlace> {
        val candidates = (pool + MapSearchCatalog.nearbyBuckets(place).all())
            .distinctBy { it.id }
            .filter { it.id != place.id && it.category == place.category }
            .filter { it.resolvedHours().isOpenAt(arriveEpochMs) }
            .take(3)
        return candidates
    }

    private fun estimateTravelMinutes(
        place: MapPlace,
        visitIndex: Int,
        selected: List<com.peyya.app.domain.Stop>,
    ): Int {
        val from = selected.getOrNull(visitIndex - 1) ?: MapSearchCatalog.home.toStop()
        return TripThemeAnalyzer.legEta(from, place.toStop()).durationMinutes.coerceAtLeast(5)
    }

    fun formatClock(epochMs: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        return String.format(
            Locale.KOREA,
            "%d:%02d",
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
        )
    }

    fun formatDepartLabel(epochMs: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        return String.format(
            Locale.KOREA,
            "%d월 %d일 %d:%02d",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
        )
    }

    private fun dayLabel(day: Int): String = when (day) {
        Calendar.SUNDAY -> "일요일"
        Calendar.MONDAY -> "월요일"
        Calendar.TUESDAY -> "화요일"
        Calendar.WEDNESDAY -> "수요일"
        Calendar.THURSDAY -> "목요일"
        Calendar.FRIDAY -> "금요일"
        Calendar.SATURDAY -> "토요일"
        else -> "이날"
    }
}
