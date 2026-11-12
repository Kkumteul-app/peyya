package com.peyya.app.data

import com.peyya.app.domain.OpeningHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class VisitFeasibilityAnalyzerTest {

    private fun epoch(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun museumRoute() = listOf(
        MapSearchCatalog.home,
        MapSearchCatalog.searchablePlaces.first { it.id == "museum" },
    )

    @Test
    fun mondayDepart_museumClosed_withAlternatives() {
        // 2026-11-16 is Monday
        val depart = epoch(2026, 11, 16, 10, 0)
        val report = VisitFeasibilityAnalyzer.evaluate(
            routeStops = museumRoute(),
            departAtEpochMs = depart,
        )
        val museum = report.checks.first { it.placeId == "museum" }
        assertEquals(VisitFeasibilityAnalyzer.Status.Closed, museum.status)
        assertTrue(museum.alternatives.isNotEmpty())
        assertTrue(report.issueCount >= 1)
    }

    @Test
    fun weekdayMorning_museumOk() {
        // 2026-11-17 is Tuesday
        val depart = epoch(2026, 11, 17, 10, 0)
        val report = VisitFeasibilityAnalyzer.evaluate(
            routeStops = museumRoute(),
            departAtEpochMs = depart,
        )
        val museum = report.checks.first { it.placeId == "museum" }
        assertEquals(VisitFeasibilityAnalyzer.Status.Ok, museum.status)
        assertEquals(0, report.issueCount)
    }

    @Test
    fun lateEvening_museumTooLate() {
        val depart = epoch(2026, 11, 17, 19, 30)
        val report = VisitFeasibilityAnalyzer.evaluate(
            routeStops = museumRoute(),
            departAtEpochMs = depart,
        )
        val museum = report.checks.first { it.placeId == "museum" }
        assertEquals(VisitFeasibilityAnalyzer.Status.TooLate, museum.status)
    }

    @Test
    fun openingHours_labelIncludesNote() {
        val hours = OpeningHours.daily(
            openHour = 10,
            closeHour = 18,
            closedWeekdays = setOf(Calendar.MONDAY),
            noteKo = "월요일 휴관",
        )
        val monday = epoch(2026, 11, 16, 12, 0)
        assertTrue(hours.labelFor(monday).contains("휴"))
    }
}
