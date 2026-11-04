package com.peyya.app.adapt

/**
 * 우회·주차 후보.
 * [isDemoEstimate] true면 UI에 「시연용/추정」 표기.
 */
data class AdaptCandidate(
    val id: String,
    val name: String,
    val kind: AdaptKind,
    val latitude: Double,
    val longitude: Double,
    val poiId: String,
    val address: String = "",
    val isDemoEstimate: Boolean = true,
)
