package com.peyya.app.viewmodel

import com.peyya.app.adapt.AdaptCandidate
import com.peyya.app.adapt.AdaptEngine
import com.peyya.app.adapt.AdaptSuggestion
import com.peyya.app.adapt.suggestionForContext
import com.peyya.app.adapt.toNaviStop
import com.peyya.app.domain.TripPhase
import com.peyya.app.service.NaviGateway
import com.peyya.app.voice.DrivingEditLock
import com.peyya.app.voice.VoiceActionBridge
import com.peyya.app.voice.VoiceCommandSimulator
import com.peyya.app.voice.VoiceTripAction

/** 적응 제안·음성 시뮬 세션 상태. ViewModel이 소유. */
class AdaptVoiceSession(
    private val navi: NaviGateway,
    private val voiceSim: VoiceCommandSimulator = VoiceCommandSimulator(),
) {
    private var adaptDismissedForStopId: String? = null

    fun reset() {
        adaptDismissedForStopId = null
        voiceSim.clear()
    }

    fun syncSuggestion(phase: TripPhase): AdaptSuggestion? {
        val stopId = when (phase) {
            is TripPhase.Arrived -> phase.stopId
            is TripPhase.AwaitingNext -> phase.nextStopId
            else -> {
                return null
            }
        }
        if (adaptDismissedForStopId == stopId) return null
        return AdaptEngine.suggestionForContext(stopId)
    }

    fun findCandidate(suggestion: AdaptSuggestion, candidateId: String): AdaptCandidate? =
        (suggestion.detours + suggestion.parking).firstOrNull { it.id == candidateId }

    fun acceptCandidate(suggestion: AdaptSuggestion, candidateId: String): AcceptResult {
        val candidate = findCandidate(suggestion, candidateId)
            ?: return AcceptResult.Ignored
        val result = navi.requestRoute(candidate.toNaviStop())
        adaptDismissedForStopId = suggestion.originalStopId
        return if (result.isSuccess) {
            AcceptResult.Routed(candidate.name)
        } else {
            AcceptResult.Failed(candidate.name)
        }
    }

    fun dismiss(suggestion: AdaptSuggestion?) {
        adaptDismissedForStopId = suggestion?.originalStopId
    }

    fun onVoiceUtterance(phase: TripPhase, utterance: String): VoiceEffect {
        if (!DrivingEditLock.isVoiceCommandAllowed(phase)) {
            return VoiceEffect.Message("지금은 음성 명령을 쓸 수 없어요.")
        }
        val command = voiceSim.simulate(utterance)
        return when (VoiceActionBridge.toTripAction(command)) {
            VoiceTripAction.ConfirmNext -> VoiceEffect.ConfirmNext
            VoiceTripAction.SkipNext -> VoiceEffect.SkipNext
            VoiceTripAction.Keep -> VoiceEffect.Keep
            VoiceTripAction.None -> VoiceEffect.Message("알아듣지 못했어요. 다음·건너뛰기·유지를 말해 주세요.")
        }
    }

    sealed interface AcceptResult {
        data object Ignored : AcceptResult
        data class Routed(val name: String) : AcceptResult
        data class Failed(val name: String) : AcceptResult
    }

    sealed interface VoiceEffect {
        data object ConfirmNext : VoiceEffect
        data object SkipNext : VoiceEffect
        data object Keep : VoiceEffect
        data class Message(val text: String) : VoiceEffect
    }
}
