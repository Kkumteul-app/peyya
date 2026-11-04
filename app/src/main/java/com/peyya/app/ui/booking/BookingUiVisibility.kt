package com.peyya.app.ui.booking

import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan

/** Idle·상세(현재 스톱)에서 예매 체크리스트를 보일지. */
object BookingUiVisibility {
    const val NO_IN_APP_PAYMENT: String =
        "인앱 결제는 없습니다. 외부 예매 페이지로 연결됩니다."

    fun bookingStopsForIdle(trip: TripPlan): List<Stop> =
        trip.stops.filter { it.needsBooking && it.prepChecklist.isNotEmpty() }

    fun detailBookingStop(phase: TripPhase, trip: TripPlan): Stop? {
        if (phase is TripPhase.Idle || phase is TripPhase.Completed) return null
        val stop = when (phase) {
            is TripPhase.Navigating -> trip.stops.firstOrNull { it.id == phase.stopId }
            is TripPhase.Arrived -> trip.stops.firstOrNull { it.id == phase.stopId }
            is TripPhase.AwaitingNext -> trip.stops.firstOrNull { it.id == phase.nextStopId }
            else -> null
        }
        return stop?.takeIf { it.needsBooking }
    }

    fun visibleBookingStops(phase: TripPhase, trip: TripPlan): List<Stop> =
        when (phase) {
            TripPhase.Idle -> bookingStopsForIdle(trip)
            else -> listOfNotNull(detailBookingStop(phase, trip))
        }

    fun showsBookingPanel(phase: TripPhase, trip: TripPlan): Boolean =
        visibleBookingStops(phase, trip).isNotEmpty()

    fun canOpenBookingUrl(stop: Stop): Boolean =
        stop.needsBooking && !stop.bookingUrl.isNullOrBlank()
}
