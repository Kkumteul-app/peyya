package com.peyya.app.domain

data class Stop(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val poiId: String,
    val poiSubId: String = "0",
    val selected: Boolean = true,
    /** 예매·예약이 필요한 스톱이면 true. 인앱 결제는 없음. */
    val needsBooking: Boolean = false,
    /** 외부 예매 페이지 딥링크. needsBooking일 때만 채운다. */
    val bookingUrl: String? = null,
    /** 출발 전 준비 체크리스트 (예: 확인증, 주차). */
    val prepChecklist: List<String> = emptyList(),
)
