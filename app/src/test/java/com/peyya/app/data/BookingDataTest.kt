package com.peyya.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingDataTest {

    @Test
    fun museum_needsBooking_true_withUrlAndChecklist() {
        val museum = PresetTrips.weekendMuseumTrip.stops.first { it.id == "museum" }

        assertTrue(museum.needsBooking)
        assertNotNull(museum.bookingUrl)
        assertTrue(museum.bookingUrl!!.startsWith("https://"))
        assertTrue(museum.prepChecklist.isNotEmpty())
    }

    @Test
    fun restaurant_needsBooking_false() {
        val restaurant = PresetTrips.weekendMuseumTrip.stops.first { it.id == "restaurant" }

        assertFalse(restaurant.needsBooking)
        assertEquals(null, restaurant.bookingUrl)
        assertTrue(restaurant.prepChecklist.isEmpty())
    }

    @Test
    fun museum_bookingUrl_pointsToOfficialMuseumSite() {
        val museum = PresetTrips.weekendMuseumTrip.stops.first { it.id == "museum" }

        assertTrue(museum.bookingUrl!!.contains("museum.go.kr"))
    }
}
