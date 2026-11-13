package com.peyya.app.domain

import com.peyya.app.data.PresetTrips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContestEnginesTest {
    @Test
    fun museumNeedsBooking() {
        val museum = PresetTrips.weekendMuseumTrip.stops.first { it.id == "museum" }
        assertTrue(museum.needsBooking)
        assertFalse(museum.bookingUrl.isNullOrBlank())
        assertTrue(museum.prepChecklist.isNotEmpty())
    }

    @Test
    fun adaptEngine_returnsDemoSuggestions() {
        val list = AdaptEngine.suggestionsOnArrive("museum")
        assertTrue(list.isNotEmpty())
        assertTrue(list.all { it.demoEstimated })
    }

    @Test
    fun voiceSim_parsesChips() {
        assertEquals(VoiceCommand.Next, VoiceCommandSimulator.parse("다음으로"))
        assertEquals(VoiceCommand.Skip, VoiceCommandSimulator.parse("건너뛰기"))
        assertEquals(VoiceCommand.Keep, VoiceCommandSimulator.parse("유지"))
    }

    @Test
    fun sentiment_and_recommender() {
        assertEquals(Sentiment.Positive, SentimentLabeler.label(5, "최고예요"))
        assertEquals(Sentiment.Negative, SentimentLabeler.label(1, "별로"))
        val store = ReviewStore().also { it.loadSeed() }
        assertTrue(store.completedTripCount() >= CourseRecommender.M)
        val courses = CourseRecommender.recommend(store, PresetTrips.weekendMuseumTrip.stops)
        assertTrue(courses.isNotEmpty())
    }
}
