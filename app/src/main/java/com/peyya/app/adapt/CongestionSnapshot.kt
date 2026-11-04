package com.peyya.app.adapt

/**
 * 스톱별 혼잡 스냅샷.
 * MVP는 목데이터; [isDemoEstimate]로 시연용/추정 표기.
 */
data class CongestionSnapshot(
    val stopId: String,
    val level: CongestionLevel,
    val waitMinutes: Int? = null,
    val isDemoEstimate: Boolean = true,
)
