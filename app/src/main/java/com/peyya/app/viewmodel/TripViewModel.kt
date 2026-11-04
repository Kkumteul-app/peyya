package com.peyya.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peyya.app.data.PresetTrips
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.progress.CoordinatorState
import com.peyya.app.progress.OnTripCompleted
import com.peyya.app.progress.SnoozeChoice
import com.peyya.app.progress.TripProgressCoordinator
import com.peyya.app.progress.TripProgressStore
import com.peyya.app.review.ActiveReviewUi
import com.peyya.app.review.CourseCandidate
import com.peyya.app.review.CourseRecommender
import com.peyya.app.review.ReviewFlowActions
import com.peyya.app.review.ReviewSessionStarter
import com.peyya.app.review.ReviewStore
import com.peyya.app.review.ReviewSyncPort
import com.peyya.app.review.ReviewUiStep
import com.peyya.app.service.IgnitionGateway
import com.peyya.app.service.NaviGateway
import com.peyya.app.service.SpeechPort
import com.peyya.app.voice.DrivingEditLock
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
 * 축2 리뷰·코스 추천은 [ReviewStore] / [CourseRecommender]를 소비한다.
 */
class TripViewModel(
    private val navi: NaviGateway,
    private val ignition: IgnitionGateway,
    private val speech: SpeechPort,
    store: TripProgressStore,
    mockMode: Boolean,
    private val reviewStore: ReviewStore,
    private val reviewSessionStarter: ReviewSessionStarter,
    private val reviewSync: ReviewSyncPort = ReviewSyncPort.NoOp,
    private val courseCatalog: List<TripPlan> = listOf(PresetTrips.weekendMuseumTrip),
    onTripCompleted: OnTripCompleted? = null,
    private val clock: () -> Long = { System.currentTimeMillis() },
    today: () -> LocalDate = { LocalDate.now() },
    pollIntervalMs: Long = 5_000L,
    tickIntervalMs: Long = 1_000L,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val wiredOnTripCompleted: OnTripCompleted =
        onTripCompleted ?: OnTripCompleted { tripId -> onTripCompletedInternal(tripId) }

    private val coordinator = TripProgressCoordinator(
        navi = navi,
        ignition = ignition,
        store = store,
        scope = scope,
        clock = clock,
        today = today,
        onTripCompleted = wiredOnTripCompleted,
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

    private var reviewStartedAtEpochMs: Long = 0L
    /** 사용자가 코스 카드를 닫으면 같은 Idle 동안 재노출하지 않는다. */
    private var courseOfferDismissed: Boolean = false
    private val adaptVoice = AdaptVoiceSession(navi = navi)

    init {
        refreshCourseOffer()
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
        if (!DrivingEditLock.isEditingAllowed(_uiState.value.phase)) return
        if (_uiState.value.courseOffer != null) return
        _uiState.update { it.copy(trip = it.trip.withStopSelection(stopId, selected)) }
        coordinator.bindTrip(_uiState.value.trip)
    }

    fun startRoute() {
        startDriving()
    }

    fun startDriving() {
        coordinator.bindTrip(_uiState.value.trip)
        coordinator.startDriving()
    }

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

    fun snoozeReservation(atEpochMs: Long) {
        coordinator.setReservationAt(atEpochMs)
        coordinator.snooze(SnoozeChoice.AtReservation)
    }

    fun skipToday() {
        coordinator.skipToday()
    }

    fun onIgnition() {
        ignition.signalIgnition()
    }

    fun resetTrip() {
        val trip = PresetTrips.weekendMuseumTrip
        adaptVoice.reset()
        coordinator.reset(trip)
        _uiState.update {
            it.copy(
                trip = trip,
                review = null,
                courseAppliedConfirm = false,
                adaptSuggestion = null,
                snackbar = "일정을 초기화했습니다. 다시 시작할 수 있습니다.",
            )
        }
        refreshCourseOffer()
    }

    fun acceptAdaptCandidate(candidateId: String) {
        val suggestion = _uiState.value.adaptSuggestion ?: return
        when (val result = adaptVoice.acceptCandidate(suggestion, candidateId)) {
            AdaptVoiceSession.AcceptResult.Ignored -> Unit
            is AdaptVoiceSession.AcceptResult.Routed -> {
                _uiState.update {
                    it.copy(
                        adaptSuggestion = null,
                        snackbar = "${result.name}으로 안내합니다.",
                    )
                }
            }
            is AdaptVoiceSession.AcceptResult.Failed -> {
                _uiState.update {
                    it.copy(snackbar = "${result.name} 안내를 시작하지 못했어요.")
                }
            }
        }
    }

    fun dismissAdaptSuggestion() {
        adaptVoice.dismiss(_uiState.value.adaptSuggestion)
        _uiState.update { it.copy(adaptSuggestion = null) }
    }

    fun simulateVoiceUtterance(utterance: String) {
        when (val effect = adaptVoice.onVoiceUtterance(_uiState.value.phase, utterance)) {
            AdaptVoiceSession.VoiceEffect.ConfirmNext -> confirmNext()
            AdaptVoiceSession.VoiceEffect.SkipNext -> skipNext()
            AdaptVoiceSession.VoiceEffect.Keep -> {
                adaptVoice.dismiss(_uiState.value.adaptSuggestion)
                _uiState.update {
                    it.copy(adaptSuggestion = null, snackbar = "지금 안내를 유지합니다.")
                }
            }
            is AdaptVoiceSession.VoiceEffect.Message -> {
                _uiState.update { it.copy(snackbar = effect.text) }
            }
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

    // --- 축2 리뷰 ---

    fun beginReviewRating() {
        val review = _uiState.value.review ?: return
        if (review.step != ReviewUiStep.Intro) return
        _uiState.update { it.copy(review = ReviewFlowActions.beginRating(review)) }
    }

    fun skipReviewSession() {
        val review = _uiState.value.review ?: return
        val now = clock()
        val session = ReviewFlowActions.toSkippedSession(
            ui = review,
            startedAtEpochMs = reviewStartedAtEpochMs,
            completedAtEpochMs = now,
        )
        reviewStore.save(session)
        runCatching { reviewSync.sync(session) }
        _uiState.update { it.copy(review = null) }
        refreshCourseOffer()
    }

    fun setReviewStars(stars: Int) {
        val review = _uiState.value.review ?: return
        if (review.step != ReviewUiStep.Rating) return
        _uiState.update { it.copy(review = ReviewFlowActions.setStars(review, stars)) }
    }

    fun setReviewComment(comment: String) {
        val review = _uiState.value.review ?: return
        if (review.step != ReviewUiStep.Rating) return
        _uiState.update { it.copy(review = ReviewFlowActions.setComment(review, comment)) }
    }

    fun commitReviewStop(carMode: Boolean = false) {
        val review = _uiState.value.review ?: return
        when (
            val result = ReviewFlowActions.commitCurrentStop(
                ui = review,
                nowEpochMs = clock(),
                carModeSkipsSubmitted = carMode,
            )
        ) {
            ReviewFlowActions.CommitResult.Ignored -> Unit
            ReviewFlowActions.CommitResult.NeedsStars -> {
                _uiState.update { it.copy(snackbar = "별점을 선택해 주세요.") }
            }
            is ReviewFlowActions.CommitResult.Advanced -> {
                _uiState.update { it.copy(review = result.ui) }
            }
            is ReviewFlowActions.CommitResult.Finished -> {
                persistCompletedReview(result.collected, result.nextUi)
            }
        }
    }

    fun skipReviewStop(carMode: Boolean = false) {
        val review = _uiState.value.review ?: return
        when (
            val result = ReviewFlowActions.skipCurrentStop(
                ui = review,
                carModeSkipsSubmitted = carMode,
            )
        ) {
            is ReviewFlowActions.CommitResult.Advanced -> {
                _uiState.update { it.copy(review = result.ui) }
            }
            is ReviewFlowActions.CommitResult.Finished -> {
                persistCompletedReview(result.collected, result.nextUi)
            }
            else -> Unit
        }
    }

    fun acknowledgeReviewSubmitted() {
        val review = _uiState.value.review ?: return
        if (review.step != ReviewUiStep.Submitted) return
        returnToIdleAfterReview()
    }

    // --- 축2 코스 추천 ---

    fun dismissCourseOffer() {
        courseOfferDismissed = true
        _uiState.update { it.copy(courseOffer = null) }
    }

    fun applyCourseOffer() {
        val candidate = _uiState.value.courseOffer ?: return
        courseOfferDismissed = true
        applyCandidate(candidate)
    }

    fun acknowledgeCourseApplied() {
        _uiState.update { it.copy(courseAppliedConfirm = false) }
    }

    override fun onCleared() {
        coordinator.stop()
        navi.release()
        ignition.release()
        speech.shutdown()
        super.onCleared()
    }

    private fun onTripCompletedInternal(tripId: String) {
        if (_uiState.value.review != null) return
        val trip = _uiState.value.trip
        val session = reviewSessionStarter.start(
            tripId = tripId.ifEmpty { trip.id },
            tripTitle = trip.title,
        )
        reviewStartedAtEpochMs = session.startedAtEpochMs
        val selected = trip.selectedStops().ifEmpty { trip.stops }
        _uiState.update {
            it.copy(review = ReviewFlowActions.fromSession(session, selected))
        }
    }

    private fun persistCompletedReview(
        collected: List<com.peyya.app.review.StopReview>,
        nextUi: ActiveReviewUi?,
    ) {
        val base = _uiState.value.review ?: return
        val finishedUi = base.copy(collected = collected)
        val now = clock()
        val session = ReviewFlowActions.toCompletedSession(
            ui = finishedUi,
            startedAtEpochMs = reviewStartedAtEpochMs,
            completedAtEpochMs = now,
        )
        reviewStore.save(session)
        runCatching { reviewSync.sync(session) }
        if (nextUi == null) {
            returnToIdleAfterReview(snackbar = "리뷰를 저장했어요.")
        } else {
            _uiState.update {
                it.copy(review = nextUi.copy(collected = collected))
            }
        }
    }

    private fun returnToIdleAfterReview(snackbar: String? = "리뷰를 저장했어요.") {
        val trip = _uiState.value.trip
        courseOfferDismissed = false
        coordinator.reset(trip)
        _uiState.update {
            it.copy(
                trip = trip,
                review = null,
                snackbar = snackbar,
            )
        }
        refreshCourseOffer()
    }

    private fun applyCandidate(candidate: CourseCandidate) {
        val newTrip = TripPlan(
            id = candidate.courseId,
            title = candidate.title,
            stops = candidate.stops.map { it.copy(selected = true) },
        )
        coordinator.reset(newTrip)
        _uiState.update {
            it.copy(
                trip = newTrip,
                courseOffer = null,
                courseAppliedConfirm = true,
                review = null,
                snackbar = null,
            )
        }
    }

    private fun refreshCourseOffer() {
        _uiState.update { state ->
            if (state.review != null || courseOfferDismissed) {
                state.copy(courseOffer = null)
            } else {
                val top = CourseRecommender.recommend(reviewStore, courseCatalog).firstOrNull()
                state.copy(courseOffer = top)
            }
        }
    }

    private fun applyProgress(progress: CoordinatorState) {
        val spoken = progress.lastSpoken
        if (!spoken.isNullOrEmpty() && spoken != _uiState.value.lastSpoken) {
            speech.speak(spoken)
        }
        val adapt = adaptVoice.syncSuggestion(progress.phase)
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
                adaptSuggestion = adapt,
            )
        }
        if (progress.phase is TripPhase.Idle && _uiState.value.review == null) {
            refreshCourseOffer()
        }
    }
}
