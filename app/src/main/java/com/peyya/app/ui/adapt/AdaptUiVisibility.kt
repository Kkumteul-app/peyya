package com.peyya.app.ui.adapt

import com.peyya.app.adapt.AdaptMockCatalog
import com.peyya.app.adapt.AdaptSuggestion
import com.peyya.app.adapt.CongestionLevel
import com.peyya.app.domain.TripPhase

/** 도착·대기 맥락에서 적응 제안 패널 표시 여부. */
object AdaptUiVisibility {
    val DEMO_LABEL: String = AdaptMockCatalog.DEMO_LABEL

    fun showsAdaptPanel(phase: TripPhase, suggestion: AdaptSuggestion?): Boolean {
        if (suggestion == null) return false
        if (!hasOfferContent(suggestion)) return false
        return phase is TripPhase.Arrived || phase is TripPhase.AwaitingNext
    }

    fun hasOfferContent(suggestion: AdaptSuggestion): Boolean =
        suggestion.detours.isNotEmpty() || suggestion.parking.isNotEmpty()

    fun congestionLabelKo(level: CongestionLevel): String = when (level) {
        CongestionLevel.Low -> "여유"
        CongestionLevel.Medium -> "보통"
        CongestionLevel.High -> "혼잡"
    }

    fun demoLabelOrNull(suggestion: AdaptSuggestion): String? =
        if (suggestion.isDemoEstimate) DEMO_LABEL else null
}
