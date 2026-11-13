package com.peyya.app.viewmodel

import androidx.lifecycle.ViewModel
import com.peyya.app.data.PresetTrips
import com.peyya.app.data.SeedCatalog
import com.peyya.app.domain.AdaptEngine
import com.peyya.app.domain.CourseRecommender
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.ReviewStore
import com.peyya.app.domain.SentimentLabeler
import com.peyya.app.domain.Stop
import com.peyya.app.domain.StopReview
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.domain.TripReviewSession
import com.peyya.app.domain.VoiceCommand
import com.peyya.app.domain.VoiceCommandSimulator
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
    private val reviewStore: ReviewStore = ReviewStore().also { it.loadSeed() },
) : ViewModel() {
    private var completedCallbackFired = false

    private val _uiState = MutableStateFlow(
        TripUiState(
            family = PresetTrips.defaultFamily,
            trip = PresetTrips.weekendMuseumTrip,
            mockMode = mockMode,
            courseCandidates = CourseRecommender.recommend(
                reviewStore,
                SeedCatalog.pois.map { it.stop } + PresetTrips.weekendMuseumTrip.stops,
            ),
        ),
    )
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    fun updateFamilyName(name: String) {
        _uiState.update { state ->
            state.copy(family = state.family.copy(familyName = name.trim().ifEmpty { state.family.familyName }))
        }
    }

    fun syncFamily(family: FamilyProfile) {
        _uiState.update { it.copy(family = family) }
    }

    fun loadTrip(plan: TripPlan) {
        completedCallbackFired = false
        _uiState.update {
            it.copy(
                trip = plan,
                phase = TripPhase.Idle,
                statusMessage = "루트를 불러왔습니다. 예매·준비를 확인한 뒤 시작하세요.",
                lastSpoken = "",
                adaptSuggestions = emptyList(),
                adaptDismissed = false,
                prepChecked = emptySet(),
                reviewPhase = ReviewUiPhase.None,
                reviewDrafts = emptyList(),
                reviewIndex = 0,
            )
        }
    }

    fun toggleStop(stopId: String, selected: Boolean) {
        if (_uiState.value.phase !is TripPhase.Idle) return
        _uiState.update { it.copy(trip = it.trip.withStopSelection(stopId, selected)) }
    }

    fun togglePrepItem(item: String) {
        _uiState.update { state ->
            val next = state.prepChecked.toMutableSet()
            if (!next.add(item)) next.remove(item)
            state.copy(prepChecked = next)
        }
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
        val suggestions = AdaptEngine.suggestionsOnArrive(navigating.stopId)
        _uiState.update {
            it.copy(
                phase = TripPhase.Arrived(navigating.stopId),
                statusMessage = "도착했어요. 시연용 적응 카드를 확인하거나 시동으로 다음을 이어가세요.",
                adaptSuggestions = suggestions,
                adaptDismissed = false,
            )
        }
    }

    fun dismissAdapt() {
        _uiState.update { it.copy(adaptDismissed = true) }
    }

    fun acceptParkingSuggestion(suggestionId: String) {
        val suggestion = _uiState.value.adaptSuggestions.firstOrNull { it.id == suggestionId } ?: return
        val alt = suggestion.altStop ?: return
        val result = navi.requestRoute(alt)
        val mode = if (navi.isMock) "Mock" else "NaviHelper"
        setStatus("[$mode] ${alt.name} 주차 안내 · 시연용/추정")
        if (result.isSuccess) {
            speak("${alt.name} 주차로 안내합니다.")
        }
        _uiState.update { it.copy(adaptDismissed = true) }
    }

    fun onIgnition() {
        ignition.signalIgnition()
        when (val phase = _uiState.value.phase) {
            is TripPhase.Arrived -> proposeNextAfterArrived(phase.stopId)
            is TripPhase.AwaitingNext -> {
                val stop = findStop(phase.nextStopId) ?: return
                val message = "다음 목적지는 ${stop.name} 입니다. 안내를 시작할까요?"
                speak(message)
                setStatus(message)
            }
            else -> setStatus("도착 처리 후 시동을 눌러 다음 안내를 받으세요.")
        }
    }

    fun applyVoiceChip(label: String) {
        when (VoiceCommandSimulator.parse(label)) {
            VoiceCommand.Next -> {
                when (_uiState.value.phase) {
                    is TripPhase.AwaitingNext -> confirmNext()
                    is TripPhase.Arrived -> onIgnition()
                    else -> setStatus("지금은 음성 시뮬로 확인할 단계가 아닙니다.")
                }
            }
            VoiceCommand.Skip -> skipNext()
            VoiceCommand.Keep -> setStatus("현재 스톱을 유지합니다. (음성 시뮬)")
            VoiceCommand.Unknown -> setStatus("이해하지 못한 명령입니다.")
        }
    }

    fun resetTrip() {
        completedCallbackFired = false
        _uiState.update {
            it.copy(
                trip = PresetTrips.weekendMuseumTrip,
                phase = TripPhase.Idle,
                statusMessage = "일정을 초기화했습니다. 다시 시작할 수 있습니다.",
                lastSpoken = "",
                adaptSuggestions = emptyList(),
                adaptDismissed = false,
                prepChecked = emptySet(),
                reviewPhase = ReviewUiPhase.None,
                reviewDrafts = emptyList(),
                reviewIndex = 0,
                courseCandidates = CourseRecommender.recommend(
                    reviewStore,
                    SeedCatalog.pois.map { s -> s.stop } + PresetTrips.weekendMuseumTrip.stops,
                ),
            )
        }
    }

    fun beginReview() {
        val drafts = _uiState.value.trip.selectedStops().map {
            StopRatingDraft(stopId = it.id, stopName = it.name)
        }
        if (drafts.isEmpty()) {
            skipReview()
            return
        }
        _uiState.update {
            it.copy(reviewPhase = ReviewUiPhase.Intro, reviewDrafts = drafts, reviewIndex = 0)
        }
    }

    fun continueReviewFromIntro() {
        _uiState.update { it.copy(reviewPhase = ReviewUiPhase.Rating) }
    }

    fun updateCurrentReview(stars: Int, comment: String) {
        _uiState.update { state ->
            val idx = state.reviewIndex
            val drafts = state.reviewDrafts.toMutableList()
            if (idx in drafts.indices) {
                drafts[idx] = drafts[idx].copy(stars = stars.coerceIn(1, 5), comment = comment)
            }
            state.copy(reviewDrafts = drafts)
        }
    }

    fun nextReviewStop() {
        val state = _uiState.value
        if (state.reviewIndex >= state.reviewDrafts.lastIndex) {
            commitReview()
        } else {
            _uiState.update { it.copy(reviewIndex = it.reviewIndex + 1) }
        }
    }

    fun skipReview() {
        reviewStore.save(
            TripReviewSession(
                tripId = _uiState.value.trip.id,
                reviews = emptyList(),
                skipped = true,
            ),
        )
        refreshCourses(ReviewUiPhase.Done)
    }

    fun applyCourse(candidateId: String) {
        val candidate = _uiState.value.courseCandidates.firstOrNull { it.id == candidateId } ?: return
        val catalog = (SeedCatalog.pois.map { it.stop } + PresetTrips.weekendMuseumTrip.stops)
            .associateBy { it.id }
        val stops = candidate.stopIds.mapNotNull { catalog[it]?.copy(selected = true) }
        if (stops.isEmpty()) return
        loadTrip(TripPlan(id = candidate.id, title = candidate.title, stops = stops))
        setStatus("추천 코스를 적용했습니다.")
    }

    private fun commitReview() {
        val state = _uiState.value
        val reviews = state.reviewDrafts.map {
            StopReview(
                stopId = it.stopId,
                stopName = it.stopName,
                stars = it.stars,
                comment = it.comment,
                sentiment = SentimentLabeler.label(it.stars, it.comment),
            )
        }
        reviewStore.save(TripReviewSession(tripId = state.trip.id, reviews = reviews))
        refreshCourses(ReviewUiPhase.Done)
        setStatus("리뷰를 저장했습니다. 코스 추천을 확인해 보세요.")
    }

    private fun refreshCourses(phase: ReviewUiPhase) {
        val catalog = SeedCatalog.pois.map { it.stop } + PresetTrips.weekendMuseumTrip.stops
        _uiState.update {
            it.copy(
                reviewPhase = phase,
                courseCandidates = CourseRecommender.recommend(reviewStore, catalog),
            )
        }
    }

    private fun proposeNextAfterArrived(currentId: String) {
        val next = findNextStopAfter(currentId)
        if (next == null) {
            completeTrip()
            return
        }
        val message = "다음 목적지는 ${next.name} 입니다. 안내를 시작할까요?"
        speak(message)
        _uiState.update {
            it.copy(
                phase = TripPhase.AwaitingNext(next.id),
                statusMessage = message,
                lastSpoken = message,
                adaptSuggestions = emptyList(),
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
        _uiState.update {
            it.copy(
                phase = if (navigating) TripPhase.Navigating(stop.id) else it.phase,
                statusMessage = "[$mode] ${stop.name} 경로 요청 완료",
                adaptSuggestions = emptyList(),
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
        if (!completedCallbackFired) {
            completedCallbackFired = true
            beginReview()
        }
        _uiState.update {
            it.copy(
                phase = TripPhase.Completed,
                statusMessage = "오늘 루트를 모두 마쳤습니다. 리뷰를 남겨 주세요.",
            )
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
}
