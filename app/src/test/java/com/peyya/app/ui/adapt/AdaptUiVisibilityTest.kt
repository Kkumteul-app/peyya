package com.peyya.app.ui.adapt

import com.peyya.app.adapt.AdaptEngine
import com.peyya.app.adapt.AdaptMockCatalog
import com.peyya.app.adapt.CongestionLevel
import com.peyya.app.domain.TripPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptUiVisibilityTest {

    @Test
    fun arrivedMuseum_showsDemoAdaptPanel() {
        val suggestion = AdaptEngine.suggest("museum")
        assertTrue(AdaptUiVisibility.showsAdaptPanel(TripPhase.Arrived("museum"), suggestion))
        assertEquals(AdaptMockCatalog.DEMO_LABEL, AdaptUiVisibility.demoLabelOrNull(suggestion))
        assertTrue(AdaptUiVisibility.hasOfferContent(suggestion))
        assertEquals("혼잡", AdaptUiVisibility.congestionLabelKo(CongestionLevel.High))
    }

    @Test
    fun idle_hidesAdaptPanelEvenWithSuggestion() {
        val suggestion = AdaptEngine.suggest("museum")
        assertFalse(AdaptUiVisibility.showsAdaptPanel(TripPhase.Idle, suggestion))
    }

    @Test
    fun nullSuggestion_hidden() {
        assertFalse(AdaptUiVisibility.showsAdaptPanel(TripPhase.Arrived("museum"), null))
    }
}
