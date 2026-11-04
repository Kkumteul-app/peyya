package com.peyya.app.progress

/**
 * 축 2(리뷰·추천)용 포트. [TripPhase.Completed] 진입 시 1회 호출.
 * 리뷰 UI는 별도 스펙 — 여기서는 콜백만 정의한다.
 */
fun interface OnTripCompleted {
    fun onTripCompleted(tripId: String)
}
