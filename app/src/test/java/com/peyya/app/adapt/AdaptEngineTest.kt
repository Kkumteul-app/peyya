package com.peyya.app.adapt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptEngineTest {

    @Test
    fun highCongestion_offersAtLeastOneDetour_andKeepsRevisit() {
        val suggestion = AdaptEngine.suggest("museum")

        assertEquals(CongestionLevel.High, suggestion.congestion.level)
        assertTrue(suggestion.detours.isNotEmpty())
        assertTrue(suggestion.keepInRevisitQueue)
        assertTrue(suggestion.parking.isNotEmpty())
    }

    @Test
    fun lowCongestion_noDetour_noRevisitQueue() {
        val suggestion = AdaptEngine.suggest(
            stopId = "restaurant",
            congestion = CongestionSnapshot(
                stopId = "restaurant",
                level = CongestionLevel.Low,
                waitMinutes = 5,
                isDemoEstimate = true,
            ),
        )

        assertTrue(suggestion.detours.isEmpty())
        assertFalse(suggestion.keepInRevisitQueue)
    }

    @Test
    fun mediumCongestion_needsDetour() {
        assertTrue(AdaptEngine.needsDetour(CongestionLevel.Medium))
        assertTrue(AdaptEngine.needsDetour(CongestionLevel.High))
        assertFalse(AdaptEngine.needsDetour(CongestionLevel.Low))
    }

    @Test
    fun museumSuggestion_marksDemoEstimate() {
        val suggestion = AdaptEngine.suggest("museum")

        assertTrue(suggestion.isDemoEstimate)
        assertTrue(suggestion.congestion.isDemoEstimate)
        assertTrue(suggestion.detours.all { it.isDemoEstimate })
        assertEquals(AdaptMockCatalog.DEMO_LABEL, "시연용/추정")
    }
}
