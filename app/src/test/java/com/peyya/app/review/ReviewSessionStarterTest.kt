package com.peyya.app.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSessionStarterTest {

    @Test
    fun start_savesInProgressSession_withTripId() {
        val store = InMemoryReviewStore()
        val starter = ReviewSessionStarter(
            store = store,
            clock = { 1_700_000_000_000L },
            idFactory = { "session-fixed" },
        )

        val session = starter.start(tripId = "weekend-museum", tripTitle = "주말 박물관 · 맛집 루트")

        assertEquals("session-fixed", session.sessionId)
        assertEquals("weekend-museum", session.tripId)
        assertEquals(TripReviewSessionStatus.InProgress, session.status)
        assertTrue(session.stopReviews.isEmpty())
        assertNull(session.completedAtEpochMs)
        assertEquals(1, store.loadSessions().size)
        assertEquals(session, store.loadSessions().single())
    }

    @Test
    fun start_twice_createsTwoSessions() {
        val store = InMemoryReviewStore()
        var n = 0
        val starter = ReviewSessionStarter(
            store = store,
            clock = { 1L },
            idFactory = { "s-${n++}" },
        )

        starter.start("t1", "A")
        starter.start("t1", "A")

        assertEquals(2, store.loadSessions().size)
    }
}
