package com.peyya.app.domain

/**
 * 마이 탭 — 운전자·차량 프로필.
 */
data class UserProfile(
    val displayName: String,
    val vehicleModel: String,
    val vehiclePlate: String,
    val vehicleNote: String = "",
)
