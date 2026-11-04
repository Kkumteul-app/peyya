package com.peyya.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peyya.app.data.PresetTrips
import com.peyya.app.progress.CoordinatorState
import com.peyya.app.progress.OnTripCompleted
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.progress.TripProgressCoordinator
import com.peyya.app.progress.TripProgressStore
import com.peyya.app.domain.TripPhase
import com.peyya.app.service.IgnitionGateway
import com.peyya.app.service.NaviGateway
import com.peyya.app.service.SpeechPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * UI 바인딩 + Coordinator 이벤트 구독.
 * phase/타이머 규칙은 [TripProgressCoordinator]가 소유한다.
 *
 * - [startRoute] / [startDriving]: 운전 시작
 * - [markArrived]: 메뉴 수동 도착 (주 CTA 아님)
 * - [confirmArrivalFromPrompt]: GPS 미확정 알림 확인
 * - [onIgnition]: UI 데모 시동 신호 (실시동 Flow와 병행)
 */
class TripViewModel(
    private val navi: NaviGateway,
    private val ignition: IgnitionGateway,
    private val speech: SpeechPort,
    store: TripProgressStore,
    mockMode: Boolean,
    onTripCompleted: OnTripCompleted = OnTripCompleted { },
    clock: () -> Long = { System.currentTimeMillis() },
    today: () -> LocalDate = { LocalDate.now() },
    pollIntervalMs: Long = 5_000L,
    tickIntervalMs: Long = 1_000L,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val coordinator = TripProgressCoordinator(
        navi = navi,
        ignition = ignition,
        store = store,
        scope = scope,
        clock = clock,
        today = today,
        onTripCompleted = onTripCompleted,
        pollIntervalMs = pollIntervalMs,
        tickIntervalMs = tickIntervalMs,
    )

    private val _uiState = MutableStateFlow(
        TripUiState(
            family = PresetTrips.defaultFamily,
            trip = PresetTrips.weekendMuseumTrip,
            mockMode = mockMode,
        ),
    )
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    init {
        coordinator.bindTrip(_uiState.value.trip)
        scope.launch {
            coordinator.state.collect { progress -> applyProgress(progress) }
        }
        coordinator.start()
    }

    fun updateFamilyName(name: String) {
        _uiState.update { state ->
            state.copy(family = state.family.copy(familyName = name.trim().ifEmpty { state.family.familyName }))
        }
    }

    fun toggleStop(stopId: String, selected: Boolean) {
        if (_uiState.value.phase !is TripPhase.Idle) return
        _uiState.update { it.copy(trip = it.trip.withStopSelection(stopId, selected)) }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 운전 시작 (시동 리마인드 CTA / Idle 주 CTA). */
    fun startRoute() {
        startDriving()
    }

    fun startDriving() {
        coordinator.bindTrip(_uiState.value.trip)
        coordinator.startDriving()
    }

    /**
     * 메뉴 「도착으로 표시」.
     * 안내 중 주 영역의 상시 「도착했어요」가 아닌 오버플로/설정용.
     */
    fun markArrived() {
        markArrivedFromMenu()
    }

    fun markArrivedFromMenu() {
        coordinator.markArrivedFromMenu()
    }

    fun confirmArrivalFromPrompt() {
        coordinator.confirmArrivalFromPrompt()
    }

    fun dismissArrivalConfirm() {
        coordinator.dismissArrivalConfirm()
    }

    fun onGpsSample(atDestination: Boolean) {
        coordinator.onGpsSample(atDestination)
    }

    fun snooze(choice: SnoozeChoice) {
        coordinator.snooze(choice)
    }

    fun skipToday() {
        coordinator.skipToday()
    }

    /**
     * UI 데모 「시동 걸기」.
     * Mock에서는 [IgnitionGateway.signalIgnition]이 ON Flow를 갱신하고,
     * Coordinator가 NextLeg / 리마인드 정책을 적용한다.
     */
    fun onIgnition() {
        ignition.signalIgnition()
    }

    fun resetTrip() {
        val trip = PresetTrips.weekendMuseumTrip
        coordinator.reset(trip)
        _uiState.update {
            it.copy(
                trip = trip,
                snackbar = "일정을 초기화했습니다. 다시 시작할 수 있습니다.",
            )
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbar = null) }
    }

    fun confirmNext() {
        coordinator.confirmNext()
    }

    fun skipNext() {
        coordinator.skipNext()
    }

    override fun onCleared() {
        coordinator.stop()
        navi.release()
        ignition.release()
        speech.shutdown()
        super.onCleared()
    }

    private fun applyProgress(progress: CoordinatorState) {
        val spoken = progress.lastSpoken
        if (!spoken.isNullOrEmpty() && spoken != _uiState.value.lastSpoken) {
            speech.speak(spoken)
        }
        _uiState.update { state ->
            state.copy(
                phase = progress.phase,
                notice = progress.notice,
                lastSpoken = spoken ?: state.lastSpoken,
                ignitionPromptVisible = progress.ignitionPromptVisible,
                arrivalConfirmVisible = progress.arrivalConfirmVisible,
                snoozeUntilEpochMs = progress.snoozeUntilEpochMs,
                skipTodayDate = progress.skipTodayDate,
                lastArrivalPath = progress.lastArrivalPath,
            )
        }
    }
}
