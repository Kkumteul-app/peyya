package com.peyya.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPhase
import com.peyya.app.service.IgnitionGateway
import com.peyya.app.service.NaviGateway
import com.peyya.app.service.SpeechGuide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TripViewModel(
    private val navi: NaviGateway,
    private val ignition: IgnitionGateway,
    private val speech: SpeechGuide,
    mockMode: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TripUiState(
            family = PresetTrips.defaultFamily,
            trip = PresetTrips.weekendMuseumTrip,
            mockMode = mockMode,
        ),
    )
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    fun updateFamilyName(name: String) {
        _uiState.update { state ->
            state.copy(family = state.family.copy(familyName = name.trim().ifEmpty { state.family.familyName }))
        }
    }

    fun toggleStop(stopId: String, selected: Boolean) {
        if (_uiState.value.phase !is TripPhase.Idle) return
        _uiState.update { it.copy(trip = it.trip.withStopSelection(stopId, selected)) }
    }

    fun startRoute() {
        val stops = _uiState.value.trip.selectedStops()
        val first = stops.firstOrNull()
        if (first == null) {
            setStatus("선택된 경유지가 없습니다.")
            return
        }
        routeTo(first, navigating = true)
    }

    fun markArrived() {
        val navigating = _uiState.value.phase as? TripPhase.Navigating ?: return
        _uiState.update {
            it.copy(
                phase = TripPhase.Arrived(navigating.stopId),
                statusMessage = "도착했어요. 시동 걸면 다음을 부를게요.",
            )
        }
    }

    fun onIgnition() {
        ignition.signalIgnition()
        when (val phase = _uiState.value.phase) {
            is TripPhase.Arrived -> proposeNextAfterArrived(phase.stopId)
            is TripPhase.AwaitingNext -> {
                val stop = findStop(phase.nextStopId) ?: return
                val message = "다음 목적지는 ${stop.name}입니다. 안내를 시작할까요?"
                speak(message)
                setStatus(message)
            }
            else -> setStatus("도착 처리 후 시동을 눌러 다음 안내를 받으세요.")
        }
    }

    fun resetTrip() {
        _uiState.update {
            it.copy(
                trip = PresetTrips.weekendMuseumTrip,
                phase = TripPhase.Idle,
                statusMessage = "일정을 초기화했습니다. 다시 시작할 수 있습니다.",
                lastSpoken = "",
            )
        }
    }

    private fun proposeNextAfterArrived(currentId: String) {
        val next = findNextStopAfter(currentId)
        if (next == null) {
            completeTrip()
            return
        }
        val message = "다음 목적지는 ${next.name}입니다. 안내를 시작할까요?"
        speak(message)
        _uiState.update {
            it.copy(
                phase = TripPhase.AwaitingNext(next.id),
                statusMessage = message,
                lastSpoken = message,
            )
        }
    }

    fun confirmNext() {
        val awaiting = _uiState.value.phase as? TripPhase.AwaitingNext ?: return
        val stop = findStop(awaiting.nextStopId) ?: return
        routeTo(stop, navigating = true)
    }

    fun skipNext() {
        when (val phase = _uiState.value.phase) {
            is TripPhase.AwaitingNext -> skipFrom(phase.nextStopId)
            is TripPhase.Arrived -> {
                val next = findNextStopAfter(phase.stopId)
                if (next == null) completeTrip() else skipFrom(next.id)
            }
            else -> setStatus("건너뛸 다음 목적지가 없습니다.")
        }
    }

    override fun onCleared() {
        navi.release()
        ignition.release()
        speech.shutdown()
        super.onCleared()
    }

    private fun routeTo(stop: Stop, navigating: Boolean) {
        val result = navi.requestRoute(stop)
        if (result.isFailure) {
            setStatus("경로 요청 실패: ${result.exceptionOrNull()?.message}")
            return
        }
        val mode = if (navi.isMock) "Mock" else "NaviHelper"
        Log.d(TAG, "route requested mode=$mode stop=${stop.id}")
        _uiState.update {
            it.copy(
                phase = if (navigating) TripPhase.Navigating(stop.id) else it.phase,
            )
        }
    }

    private fun findNextStopAfter(stopId: String): Stop? {
        val selected = _uiState.value.trip.selectedStops()
        val index = selected.indexOfFirst { it.id == stopId }
        if (index < 0 || index >= selected.lastIndex) return null
        return selected[index + 1]
    }

    private fun findStop(stopId: String): Stop? =
        _uiState.value.trip.stops.firstOrNull { it.id == stopId }

    private fun skipFrom(stopId: String) {
        val next = findNextStopAfter(stopId)
        if (next == null) {
            completeTrip()
            return
        }
        val message = "다음 ${next.name} 안내를 건너뛰고 대기합니다."
        _uiState.update {
            it.copy(phase = TripPhase.AwaitingNext(next.id), statusMessage = message)
        }
    }

    private fun completeTrip() {
        _uiState.update {
            it.copy(phase = TripPhase.Completed, statusMessage = "오늘 루트를 모두 마쳤습니다.")
        }
        speak("오늘 루트를 모두 마쳤습니다.")
    }

    private fun speak(message: String) {
        speech.speak(message)
        _uiState.update { it.copy(lastSpoken = message) }
    }

    private fun setStatus(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    companion object {
        private const val TAG = "PeyyaTrip"
    }
}
