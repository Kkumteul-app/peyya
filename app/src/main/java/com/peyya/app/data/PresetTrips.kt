package com.peyya.app.data

import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPlan

/**
 * Demo POI data for contest MVP / phone screen recording.
 * Museum coords approximate official location; restaurant is a nearby invented POI
 * (poiId peyya-demo-restaurant — not a live booking target).
 */
object PresetTrips {
    val defaultFamily = FamilyProfile(
        familyName = "김씨 가족",
        members = listOf(
            FamilyMember("아빠", AgeBand.ADULT),
            FamilyMember("엄마", AgeBand.ADULT),
            FamilyMember("지우", AgeBand.CHILD),
            FamilyMember("할머니", AgeBand.SENIOR),
        ),
    )

    val weekendMuseumTrip = TripPlan(
        id = "weekend-museum",
        title = "주말 박물관 · 맛집 루트",
        stops = listOf(
            Stop(
                id = "museum",
                name = "국립중앙박물관",
                address = "서울 용산구 서빙고로 137",
                latitude = 37.52386,
                longitude = 126.98047,
                poiId = "peyya-demo-museum",
            ),
            Stop(
                id = "restaurant",
                name = "용산 한식 담소",
                address = "서울 용산구 한강대로 405 인근",
                latitude = 37.53120,
                longitude = 126.96580,
                poiId = "peyya-demo-restaurant",
            ),
        ),
    )
}
