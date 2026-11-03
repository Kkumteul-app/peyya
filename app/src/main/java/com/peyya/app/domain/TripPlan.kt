package com.peyya.app.domain

data class TripPlan(
    val id: String,
    val title: String,
    val stops: List<Stop>,
) {
    fun selectedStops(): List<Stop> = stops.filter { it.selected }

    fun withStopSelection(stopId: String, selected: Boolean): TripPlan {
        return copy(
            stops = stops.map { stop ->
                if (stop.id == stopId) stop.copy(selected = selected) else stop
            },
        )
    }
}
