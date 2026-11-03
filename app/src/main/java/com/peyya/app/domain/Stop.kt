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
)
