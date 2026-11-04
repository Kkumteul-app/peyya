package com.peyya.app.adapt

import com.peyya.app.domain.Stop

/** 우회·주차 후보를 내비 [Stop]으로 변환. */
fun AdaptCandidate.toNaviStop(): Stop = Stop(
    id = id,
    name = name,
    address = address.ifBlank { name },
    latitude = latitude,
    longitude = longitude,
    poiId = poiId,
)

/** 도착·대기 phase의 스톱 id로 제안 생성. 내용 없으면 null. */
fun AdaptEngine.suggestionForContext(stopId: String): AdaptSuggestion? {
    val suggestion = suggest(stopId)
    val hasContent = suggestion.detours.isNotEmpty() || suggestion.parking.isNotEmpty()
    return suggestion.takeIf { hasContent }
}
