package com.peyya.app.domain

import java.util.Calendar

/**
 * 시연용 운영시간.
 * [weekdayMinutes]: Calendar.SUNDAY..SATURDAY → Pair(openMinuteOfDay, closeMinuteOfDay), null=휴무.
 */
data class OpeningHours(
    val weekdayMinutes: Map<Int, Pair<Int, Int>?>,
    val noteKo: String = "",
) {
    fun isOpenAt(epochMs: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val window = weekdayMinutes[day] ?: return false
        val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return minuteOfDay in window.first until window.second
    }

    fun labelFor(epochMs: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val window = weekdayMinutes[day]
        if (window == null) {
            return noteKo.ifBlank { "이날 휴무" }
        }
        return "${fmt(window.first)}–${fmt(window.second)}" +
            if (noteKo.isNotBlank()) " · $noteKo" else ""
    }

    companion object {
        fun minutes(hour: Int, minute: Int = 0): Int = hour * 60 + minute

        fun fmt(minuteOfDay: Int): String {
            val h = minuteOfDay / 60
            val m = minuteOfDay % 60
            return "%d:%02d".format(h, m)
        }

        /** 매일 동일 시간, [closedWeekdays]는 Calendar 상수 */
        fun daily(
            openHour: Int,
            closeHour: Int,
            closedWeekdays: Set<Int> = emptySet(),
            noteKo: String = "",
        ): OpeningHours {
            val open = minutes(openHour)
            val close = minutes(closeHour)
            val map = (Calendar.SUNDAY..Calendar.SATURDAY).associateWith { day ->
                if (day in closedWeekdays) null else open to close
            }
            return OpeningHours(weekdayMinutes = map, noteKo = noteKo)
        }

        fun alwaysOpen(): OpeningHours = daily(0, 24)
    }
}

fun PlaceCategory.defaultDwellMinutes(): Int = when (this) {
    PlaceCategory.CULTURE -> 90
    PlaceCategory.FOOD -> 60
    PlaceCategory.CAFE -> 40
    PlaceCategory.OUTDOOR -> 50
    PlaceCategory.ANCHOR -> 20
}

fun PlaceCategory.defaultOpeningHours(): OpeningHours = when (this) {
    PlaceCategory.CULTURE -> OpeningHours.daily(
        openHour = 10,
        closeHour = 18,
        closedWeekdays = setOf(Calendar.MONDAY),
        noteKo = "월요일 휴관",
    )
    PlaceCategory.FOOD -> OpeningHours.daily(11, 21)
    PlaceCategory.CAFE -> OpeningHours.daily(9, 22)
    PlaceCategory.OUTDOOR -> OpeningHours.daily(6, 22)
    PlaceCategory.ANCHOR -> OpeningHours.alwaysOpen()
}
