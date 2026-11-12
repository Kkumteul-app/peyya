package com.peyya.app.progress

import com.peyya.app.domain.Stop
import com.peyya.app.data.DestinationGuideSpeech
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.service.IgnitionGateway
import com.peyya.app.service.IgnitionState
import com.peyya.app.service.NaviGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 여정 phase 단일 갱신기.
 * ArrivalVerifier · IgnitionPromptPolicy · NextLegPrompt · TripProgressStore 를 조합한다.
 * UI 타이머를 Composable/ViewModel에 두지 않는다.
 */
class TripProgressCoordinator(
    private val navi: NaviGateway,
    private val ignition: IgnitionGateway,
    private val store: TripProgressStore,
    private val scope: CoroutineScope,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val today: () -> LocalDate = { LocalDate.now() },
    private val arrivalVerifier: ArrivalVerifier = ArrivalVerifier(clock),
    private val nextLegPrompt: NextLegPrompt = NextLegPrompt(),
    private val onTripCompleted: OnTripCompleted = OnTripCompleted { },
    private val pollIntervalMs: Long = 5_000L,
    private val tickIntervalMs: Long = 1_000L,
) {
    private val _state = MutableStateFlow(CoordinatorState())
    val state: StateFlow<CoordinatorState> = _state.asStateFlow()

    private var selectedStops: List<Stop> = emptyList()
    private var ignitionOn: Boolean = false
    private var gpsDwellStartedAt: Long? = null
    private var completedCallbackFired: Boolean = false
    private var started: Boolean = false
    private val jobs = mutableListOf<Job>()

    fun bindTrip(trip: TripPlan) {
        selectedStops = trip.selectedStops()
        _state.update { it.copy(tripId = trip.id) }
    }

    fun start() {
        if (started) return
        started = true
        restoreFromStore()
        jobs += scope.launch { collectArrivalEvents() }
        jobs += scope.launch { pollGuidanceLoop() }
        jobs += scope.launch { collectIgnition() }
        jobs += scope.launch { tickLoop() }
    }

    fun stop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        started = false
    }

    /** 「운전 시작」— 시동 리마인드와 별개로 안내를 시작한다. */
    fun startDriving(): Boolean {
        val first = selectedStops.firstOrNull()
        if (first == null) {
            _state.update { it.copy(notice = "선택된 경유지가 없습니다.") }
            return false
        }
        val result = navi.requestRoute(first)
        if (result.isFailure) {
            _state.update { it.copy(notice = "길안내를 시작하지 못했어요. 다시 시도해 주세요.") }
            return false
        }
        completedCallbackFired = false
        gpsDwellStartedAt = null
        setPhase(
            phase = TripPhase.Navigating(first.id),
            stopId = first.id,
            notice = null,
            ignitionPromptVisible = false,
            arrivalConfirmVisible = false,
            nextLegProposed = false,
            lastArrivalPath = null,
            lastSpoken = DestinationGuideSpeech.forStop(first),
        )
        return true
    }

    /** 메뉴 「도착으로 표시」전용. 주 CTA가 아님. */
    fun markArrivedFromMenu() {
        val navigating = _state.value.phase as? TripPhase.Navigating ?: return
        applyArrival(navigating.stopId, arrivalVerifier.onMenuConfirm())
    }

    /** GPS 미확정 알림의 「도착 완료」. */
    fun confirmArrivalFromPrompt() {
        val navigating = _state.value.phase as? TripPhase.Navigating ?: return
        if (!_state.value.arrivalConfirmVisible) return
        applyArrival(navigating.stopId, arrivalVerifier.onUserConfirmFromPrompt())
    }

    fun dismissArrivalConfirm() {
        _state.update { it.copy(arrivalConfirmVisible = false) }
        persist()
    }

    /**
     * GPS 샘플. [atDestination]이면 체류 시작을 기록하고,
     * 체류 [ArrivalVerifier] 기준 초과 시 자동 Arrived 없이 확인 플래그만 켠다.
     */
    fun onGpsSample(atDestination: Boolean) {
        if (_state.value.phase !is TripPhase.Navigating) return
        if (atDestination) {
            if (gpsDwellStartedAt == null) gpsDwellStartedAt = clock()
        } else {
            gpsDwellStartedAt = null
        }
        when (arrivalVerifier.onGpsSample(atDestination, gpsDwellStartedAt)) {
            ArrivalDecision.NeedsUserConfirm -> {
                _state.update {
                    it.copy(
                        arrivalConfirmVisible = true,
                        notice = "도착 완료가 확인되지 않았어요. 도착했다면 알려 주세요.",
                    )
                }
                persist()
            }
            else -> Unit
        }
    }

    fun snooze(choice: SnoozeChoice) {
        val until = IgnitionPromptPolicy.applySnooze(
            choice = choice,
            now = clock(),
            reservationAt = _state.value.reservationAtEpochMs,
        )
        _state.update {
            it.copy(
                snoozeUntilEpochMs = until,
                ignitionPromptVisible = false,
            )
        }
        persist()
    }

    fun skipToday() {
        _state.update {
            it.copy(
                skipTodayDate = today(),
                ignitionPromptVisible = false,
            )
        }
        persist()
    }

    fun setReservationAt(epochMs: Long?) {
        _state.update { it.copy(reservationAtEpochMs = epochMs) }
        persist()
    }

    fun confirmNext(): Boolean {
        val awaiting = _state.value.phase as? TripPhase.AwaitingNext ?: return false
        val stop = selectedStops.firstOrNull { it.id == awaiting.nextStopId } ?: return false
        val result = navi.requestRoute(stop)
        if (result.isFailure) {
            _state.update { it.copy(notice = "길안내를 시작하지 못했어요. 다시 시도해 주세요.") }
            return false
        }
        gpsDwellStartedAt = null
        setPhase(
            phase = TripPhase.Navigating(stop.id),
            stopId = stop.id,
            notice = null,
            arrivalConfirmVisible = false,
            nextLegProposed = false,
            lastArrivalPath = null,
            lastSpoken = DestinationGuideSpeech.forStop(stop),
        )
        return true
    }

    fun skipNext() {
        when (val phase = _state.value.phase) {
            is TripPhase.AwaitingNext -> skipFrom(phase.nextStopId)
            is TripPhase.Arrived -> {
                val next = findNextStopAfter(phase.stopId)
                if (next == null) completeTrip() else skipFrom(next.id)
            }
            else -> _state.update { it.copy(notice = "건너뛸 다음 목적지가 없습니다.") }
        }
    }

    fun reset(trip: TripPlan) {
        store.clear()
        selectedStops = trip.selectedStops()
        completedCallbackFired = false
        gpsDwellStartedAt = null
        ignitionOn = false
        _state.value = CoordinatorState(tripId = trip.id)
        refreshIgnitionPrompt()
    }

    private suspend fun collectArrivalEvents() {
        navi.arrivalEvents().collect {
            val navigating = _state.value.phase as? TripPhase.Navigating ?: return@collect
            applyArrival(navigating.stopId, arrivalVerifier.onNaviArrivalEvent())
        }
    }

    private suspend fun pollGuidanceLoop() {
        while (scope.isActive) {
            delay(pollIntervalMs)
            val navigating = _state.value.phase as? TripPhase.Navigating ?: continue
            val snapshot = navi.pollGuidance().getOrNull() ?: continue
            when (val decision = arrivalVerifier.onPoll(snapshot)) {
                is ArrivalDecision.Confirmed -> applyArrival(navigating.stopId, decision)
                else -> Unit
            }
        }
    }

    private suspend fun collectIgnition() {
        ignition.ignitionState().collect { ignitionState ->
            val wasOn = ignitionOn
            ignitionOn = ignitionState == IgnitionState.On
            val now = clock()
            if (wasOn && !ignitionOn) {
                nextLegPrompt.onIgnitionOff()
            }
            if (!wasOn && ignitionOn) {
                val arrived = _state.value.phase as? TripPhase.Arrived
                if (arrived != null && nextLegPrompt.onIgnitionOn(now)) {
                    proposeNext(arrived.stopId)
                }
            }
            refreshIgnitionPrompt()
        }
    }

    private suspend fun tickLoop() {
        while (scope.isActive) {
            delay(tickIntervalMs)
            val arrived = _state.value.phase as? TripPhase.Arrived
            if (arrived != null && nextLegPrompt.onTick(clock(), ignitionOn)) {
                proposeNext(arrived.stopId)
            }
            refreshIgnitionPrompt()
        }
    }

    private fun applyArrival(stopId: String, decision: ArrivalDecision) {
        if (_state.value.phase !is TripPhase.Navigating) return
        val confirmed = decision as? ArrivalDecision.Confirmed ?: return
        gpsDwellStartedAt = null
        nextLegPrompt.onArrived(ignitionOn = ignitionOn, atMs = clock())
        setPhase(
            phase = TripPhase.Arrived(stopId),
            stopId = stopId,
            notice = null,
            arrivalConfirmVisible = false,
            nextLegProposed = false,
            lastArrivalPath = confirmed.path,
        )
    }

    private fun proposeNext(currentStopId: String) {
        if (_state.value.phase !is TripPhase.Arrived) return
        val next = findNextStopAfter(currentStopId)
        if (next == null) {
            completeTrip()
            return
        }
        val message = "다음 목적지는 ${next.name}입니다. 안내를 시작할까요?"
        setPhase(
            phase = TripPhase.AwaitingNext(next.id),
            stopId = next.id,
            notice = null,
            nextLegProposed = true,
            lastSpoken = message,
        )
    }

    private fun skipFrom(stopId: String) {
        val next = findNextStopAfter(stopId)
        if (next == null) {
            completeTrip()
            return
        }
        setPhase(
            phase = TripPhase.AwaitingNext(next.id),
            stopId = next.id,
            notice = null,
            nextLegProposed = true,
        )
    }

    private fun completeTrip() {
        setPhase(
            phase = TripPhase.Completed,
            stopId = null,
            notice = null,
            lastSpoken = "오늘 루트를 모두 마쳤습니다.",
        )
        if (!completedCallbackFired) {
            completedCallbackFired = true
            val id = _state.value.tripId.orEmpty()
            onTripCompleted.onTripCompleted(id)
        }
    }

    private fun refreshIgnitionPrompt() {
        val snap = _state.value
        val flags = PromptFlags(
            phaseIdle = snap.phase is TripPhase.Idle,
            skipTodayDate = snap.skipTodayDate,
            snoozeUntilEpochMs = snap.snoozeUntilEpochMs,
            nowEpochMs = clock(),
            today = today(),
        )
        val visible = IgnitionPromptPolicy.evaluate(onEvent = ignitionOn, flags = flags) is
            IgnitionPromptResult.ShowPrompt
        if (snap.ignitionPromptVisible != visible) {
            _state.update { it.copy(ignitionPromptVisible = visible) }
        }
    }

    private fun setPhase(
        phase: TripPhase,
        stopId: String?,
        notice: String?,
        ignitionPromptVisible: Boolean? = null,
        arrivalConfirmVisible: Boolean? = null,
        nextLegProposed: Boolean? = null,
        lastArrivalPath: ArrivalPath? = _state.value.lastArrivalPath,
        lastSpoken: String? = null,
    ) {
        _state.update { current ->
            current.copy(
                phase = phase,
                stopId = stopId,
                notice = notice,
                ignitionPromptVisible = ignitionPromptVisible ?: current.ignitionPromptVisible,
                arrivalConfirmVisible = arrivalConfirmVisible ?: current.arrivalConfirmVisible,
                nextLegProposed = nextLegProposed ?: current.nextLegProposed,
                lastArrivalPath = lastArrivalPath,
                lastSpoken = lastSpoken ?: current.lastSpoken,
            )
        }
        if (phase is TripPhase.Idle) {
            refreshIgnitionPrompt()
        }
        persist()
    }

    private fun persist() {
        val snap = _state.value
        store.save(
            TripProgressSnapshot(
                tripId = snap.tripId,
                phase = snap.phase,
                stopId = snap.stopId,
                snoozeUntilEpochMs = snap.snoozeUntilEpochMs,
                skipTodayDate = snap.skipTodayDate,
                reservationAtEpochMs = snap.reservationAtEpochMs,
                nextLegProposed = snap.nextLegProposed,
                lastArrivalPath = snap.lastArrivalPath,
            ),
        )
    }

    private fun restoreFromStore() {
        val loaded = store.load()?.clearedSkipIfStale(today()) ?: return
        _state.value = CoordinatorState(
            phase = loaded.phase,
            tripId = loaded.tripId ?: _state.value.tripId,
            stopId = loaded.stopId,
            snoozeUntilEpochMs = loaded.snoozeUntilEpochMs,
            skipTodayDate = loaded.skipTodayDate,
            reservationAtEpochMs = loaded.reservationAtEpochMs,
            nextLegProposed = loaded.nextLegProposed,
            lastArrivalPath = loaded.lastArrivalPath,
        )
        if (loaded.phase is TripPhase.Arrived && loaded.stopId != null) {
            nextLegPrompt.onArrived(ignitionOn = ignitionOn, atMs = clock())
            if (loaded.nextLegProposed) {
                // 이미 제안된 세션 — onArrived가 proposed=false로 리셋하므로 재제안 방지용 더미 tick 가드:
                // NextLegPrompt에 proposed 복원 API가 없으므로 OFF→ON/ dwell 재노출을 막기 위해
                // 즉시 onTick/onIgnitionOn이 false가 되도록 proposed를 소비한다.
                nextLegPrompt.onIgnitionOff()
                nextLegPrompt.onIgnitionOn(clock())
            }
        }
        if (loaded.phase is TripPhase.Completed) {
            completedCallbackFired = true
        }
        refreshIgnitionPrompt()
    }

    private fun findNextStopAfter(stopId: String): Stop? {
        val index = selectedStops.indexOfFirst { it.id == stopId }
        if (index < 0 || index >= selectedStops.lastIndex) return null
        return selectedStops[index + 1]
    }
}
