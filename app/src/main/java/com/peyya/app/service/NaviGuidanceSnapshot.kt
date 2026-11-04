package com.peyya.app.service

/**
 * 내비 안내 중 관측 스냅샷.
 * [remainingMeters] 목적지까지 잔여 거리(m).
 * [isArrived] 목적지 도착 여부.
 * [rawState] SDK/시뮬 원본 상태 문자열(디버그·로그용).
 */
data class NaviGuidanceSnapshot(
    val remainingMeters: Double,
    val isArrived: Boolean,
    val rawState: String,
)
