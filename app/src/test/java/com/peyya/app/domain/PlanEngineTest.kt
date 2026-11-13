package com.peyya.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanEngineTest {
    @Test
    fun suggest_returnsVehicleStopsForYongsan() {
        val slots = PlanSlots(
            destination = "용산 박물관",
            companions = "가족 4명",
            timeBand = "오전",
            style = "여유롭게",
        )
        val candidates = PlanEngine.suggest(slots)
        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { it.stops.isNotEmpty() })
        assertTrue(candidates.all { c -> c.stops.all { it.latitude != 0.0 && it.address.isNotBlank() } })
    }

    @Test
    fun advanceSlots_reachesCandidatesAfterTimeBand() {
        var session = PlanSession()
        session = PlanEngine.advanceSlots(session, "성수")
        session = PlanEngine.advanceSlots(session, "부부")
        session = PlanEngine.advanceSlots(session, "점심")
        assertEquals(PlanPhase.Candidates, session.phase)
        assertTrue(session.candidates.isNotEmpty())
    }

    @Test
    fun toScheduledRoute_setsScheduledStatus() {
        val candidate = PlanEngine.suggest(
            PlanSlots("분당", "아이 포함", "오후", "알차게"),
        ).first()
        val route = PlanEngine.toScheduledRoute(candidate, 1_700_000_000_000L)
        assertEquals(RouteStatus.Scheduled, route.status)
        assertEquals(RouteSource.PlanEngine, route.source)
    }
}
