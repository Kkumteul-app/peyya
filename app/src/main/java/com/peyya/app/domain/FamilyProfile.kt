package com.peyya.app.domain

enum class AgeBand(val labelKo: String) {
    CHILD("어린이"),
    TEEN("청소년"),
    ADULT("성인"),
    SENIOR("시니어"),
}

data class FamilyMember(
    val name: String,
    val ageBand: AgeBand,
)

data class FamilyProfile(
    val familyName: String,
    val members: List<FamilyMember>,
)
