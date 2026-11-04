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
    /** 국립중앙박물관 공식 예약 안내 (어린이박물관·VR 등). 인앱 결제 없음. */
    const val MUSEUM_BOOKING_URL =
        "https://www.museum.go.kr/MUSEUM/contents/M0104090000.do"

    val MUSEUM_PREP_CHECKLIST: List<String> = listOf(
        "어린이박물관·특별전 예매 여부 확인",
        "모바일 예매 확인증 준비",
        "주차 위치·입차 동선 확인",
        "유모차·가방 보관소 위치 확인",
    )

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
                needsBooking = true,
                bookingUrl = MUSEUM_BOOKING_URL,
                prepChecklist = MUSEUM_PREP_CHECKLIST,
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
