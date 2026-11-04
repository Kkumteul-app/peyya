package com.peyya.app.ui.booking

import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingUiVisibilityTest {

    private val trip = PresetTrips.weekendMuseumTrip

    @Test
    fun idle_showsMuseumChecklistPanel() {
        assertTrue(BookingUiVisibility.showsBookingPanel(TripPhase.Idle, trip))
        val stops = BookingUiVisibility.visibleBookingStops(TripPhase.Idle, trip)
        assertEquals(1, stops.size)
        assertEquals("museum", stops.single().id)
        assertTrue(stops.single().prepChecklist.isNotEmpty())
        assertTrue(BookingUiVisibility.canOpenBookingUrl(stops.single()))
    }

    @Test
    fun navigatingMuseum_showsDetailBooking() {
        val stops = BookingUiVisibility.visibleBookingStops(
            TripPhase.Navigating("museum"),
            trip,
        )
        assertEquals(1, stops.size)
        assertTrue(BookingUiVisibility.NO_IN_APP_PAYMENT.contains("인앱 결제"))
    }

    @Test
    fun navigatingRestaurant_hidesBooking() {
        assertFalse(
            BookingUiVisibility.showsBookingPanel(
                TripPhase.Navigating("restaurant"),
                trip,
            ),
        )
    }
}
