package com.peyya.app.ui.voice

import com.peyya.app.domain.TripPhase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceUiVisibilityTest {

    @Test
    fun voiceSim_visibleOnlyInDrivingPhasesWithMock() {
        assertTrue(
            VoiceUiVisibility.showsVoiceSimChips(TripPhase.AwaitingNext("restaurant"), true),
        )
        assertTrue(VoiceUiVisibility.showsVoiceSimChips(TripPhase.Arrived("museum"), true))
        assertFalse(VoiceUiVisibility.showsVoiceSimChips(TripPhase.Idle, true))
        assertFalse(
            VoiceUiVisibility.showsVoiceSimChips(TripPhase.AwaitingNext("restaurant"), false),
        )
    }

    @Test
    fun editLockHint_whenNotIdle() {
        assertFalse(VoiceUiVisibility.showsEditLockHint(TripPhase.Idle))
        assertTrue(VoiceUiVisibility.showsEditLockHint(TripPhase.Navigating("museum")))
        assertTrue(VoiceUiVisibility.EDIT_LOCK_HINT.contains("주행"))
    }
}
