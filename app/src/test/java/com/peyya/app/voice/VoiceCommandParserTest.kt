package com.peyya.app.voice

import com.peyya.app.domain.TripPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun parse_next_variants() {
        assertEquals(VoiceCommand.ConfirmNext, VoiceCommandParser.parse("다음"))
        assertEquals(VoiceCommand.ConfirmNext, VoiceCommandParser.parse("다음으로"))
        assertEquals(VoiceCommand.ConfirmNext, VoiceCommandParser.parse("  다음으로  "))
        assertEquals(VoiceCommand.ConfirmNext, VoiceCommandParser.parse("안내해줘"))
    }

    @Test
    fun parse_skip_and_keep() {
        assertEquals(VoiceCommand.Skip, VoiceCommandParser.parse("건너뛰기"))
        assertEquals(VoiceCommand.Skip, VoiceCommandParser.parse("스킵"))
        assertEquals(VoiceCommand.Keep, VoiceCommandParser.parse("유지"))
        assertEquals(VoiceCommand.Keep, VoiceCommandParser.parse("그대로"))
    }

    @Test
    fun parse_unknown() {
        assertEquals(VoiceCommand.Unknown, VoiceCommandParser.parse(""))
        assertEquals(VoiceCommand.Unknown, VoiceCommandParser.parse("날씨 어때"))
    }

    @Test
    fun simulator_utterance_mapsToConfirmNext() {
        val sim = VoiceCommandSimulator()
        val command = sim.simulate("다음으로")

        assertEquals(VoiceCommand.ConfirmNext, command)
        assertEquals(VoiceTripAction.ConfirmNext, VoiceActionBridge.toTripAction(command))
        assertEquals(listOf("다음으로"), sim.uttered())
    }

    @Test
    fun driving_editLock_blocksWhileNavigating() {
        assertTrue(DrivingEditLock.isEditingAllowed(TripPhase.Idle))
        assertFalse(DrivingEditLock.isEditingAllowed(TripPhase.Navigating("museum")))
        assertFalse(DrivingEditLock.isEditingAllowed(TripPhase.AwaitingNext("restaurant")))
        assertTrue(DrivingEditLock.isVoiceCommandAllowed(TripPhase.AwaitingNext("restaurant")))
    }
}
