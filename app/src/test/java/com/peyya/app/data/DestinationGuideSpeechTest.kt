package com.peyya.app.data

import com.peyya.app.domain.Stop
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationGuideSpeechTest {
    @Test
    fun museumBriefing_mentionsParkingAndAccess() {
        val stop = Stop(
            id = "museum",
            name = "국립중앙박물관",
            address = "서울 용산구",
            latitude = 37.52,
            longitude = 126.98,
            poiId = "peyya-demo-museum",
        )
        val spoken = DestinationGuideSpeech.forStop(stop)
        assertTrue(spoken.contains("국립중앙박물관"))
        assertTrue(spoken.contains("주차"))
        assertTrue(spoken.contains("유모차") || spoken.contains("휠체어"))
    }
}
