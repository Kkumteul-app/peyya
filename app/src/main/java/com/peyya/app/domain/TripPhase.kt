package com.peyya.app.domain

sealed interface TripPhase {
    data object Idle : TripPhase

    data class Navigating(val stopId: String) : TripPhase

    data class Arrived(val stopId: String) : TripPhase

    data class AwaitingNext(val nextStopId: String) : TripPhase

    data object Completed : TripPhase
}

fun TripPhase.labelKo(): String = when (this) {
    TripPhase.Idle -> "출발 전"
    is TripPhase.Navigating -> "안내 중"
    is TripPhase.Arrived -> "도착"
    is TripPhase.AwaitingNext -> "다음 장소"
    TripPhase.Completed -> "모두 마쳤어요"
}
