package com.peyya.app.data

object PeyyaImages {
    const val ONBOARD = "file:///android_asset/images/onboard_drive.jpg"
    const val FAMILY_CAR = "file:///android_asset/images/family_car.jpg"
    const val CITY = "file:///android_asset/images/city_night.jpg"
    const val MUSEUM = "file:///android_asset/images/museum.jpg"
    const val FOOD = "file:///android_asset/images/food.jpg"
    const val CAFE = "file:///android_asset/images/cafe.jpg"
    const val SEONGSU = "file:///android_asset/images/seongsu.jpg"
    const val BRUNCH = "file:///android_asset/images/brunch.jpg"
    const val PARK = "file:///android_asset/images/park.jpg"
    const val BBQ = "file:///android_asset/images/bbq.jpg"

    fun forStopId(stopId: String): String = when {
        stopId.contains("museum", ignoreCase = true) -> MUSEUM
        stopId.contains("restaurant", ignoreCase = true) || stopId.contains("food", ignoreCase = true) -> FOOD
        stopId.contains("cafe", ignoreCase = true) -> CAFE
        stopId.contains("seongsu", ignoreCase = true) || stopId.contains("pop", ignoreCase = true) -> SEONGSU
        stopId.contains("brunch", ignoreCase = true) -> BRUNCH
        stopId.contains("park", ignoreCase = true) -> PARK
        stopId.contains("bbq", ignoreCase = true) -> BBQ
        else -> CITY
    }

    fun forRegion(region: String): String = when (region) {
        "성수" -> SEONGSU
        "분당" -> PARK
        else -> MUSEUM
    }
}
