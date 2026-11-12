package com.peyya.app.domain

/**
 * 지도 검색·주변 추천용 POI.
 */
data class MapPlace(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photoAsset: String,
    val category: PlaceCategory,
    /** 앵커로부터 직선 거리(m). 검색 결과에는 null. */
    val distanceMeters: Int? = null,
    /** 유모차 출입·보관 가능 */
    val strollerOk: Boolean = false,
    /** 아기의자(하이체어) 제공 */
    val highChairOk: Boolean = false,
    val needsBooking: Boolean = false,
    val bookingUrl: String? = null,
    val prepChecklist: List<String> = emptyList(),
    val highlights: List<String> = emptyList(),
    val parkingTips: List<String> = emptyList(),
    val familyTips: List<String> = emptyList(),
    /** null이면 카테고리 기본 운영시간 사용 */
    val openingHours: OpeningHours? = null,
) {
    val kidFriendly: Boolean get() = strollerOk || highChairOk

    fun resolvedHours(): OpeningHours = openingHours ?: category.defaultOpeningHours()

    fun familyAmenityLabel(): String? = buildList {
        if (strollerOk) add("유모차")
        if (highChairOk) add("아기의자")
    }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    fun toStop(selected: Boolean = true): Stop = Stop(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        poiId = "peyya-$id",
        selected = selected,
        needsBooking = needsBooking,
        bookingUrl = bookingUrl,
        prepChecklist = prepChecklist,
        highlights = highlights,
        parkingTips = parkingTips,
        familyTips = familyTips,
        openingHours = resolvedHours(),
        category = category,
    )
}
