package com.peyya.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peyya.app.data.MapSearchCatalog
import com.peyya.app.data.PresetTrips
import com.peyya.app.data.VisitFeasibilityAnalyzer
import com.peyya.app.data.VWorldPlaceSearch
import com.peyya.app.domain.MapPlace
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.service.ReviewCompletionNotifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import kotlin.math.pow

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
    private val courseCatalog: List<TripPlan> = PresetTrips.courseCatalog,
    private val appContext: Context? = null,
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
        run {
            val planMap = PlanMapUi.seededForMuseumTrip()
            TripUiState(
                family = PresetTrips.defaultFamily,
                trip = planMap.toTripPlan(PresetTrips.weekendMuseumTrip),
                planMap = planMap,
                mockMode = mockMode,
            )
        },
    )
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var reviewStartedAtEpochMs: Long = 0L
    /** 사용자가 코스 카드를 닫으면 같은 Idle 동안 재노출하지 않는다. */
    private var courseOfferDismissed: Boolean = false
    private val adaptVoice = AdaptVoiceSession(navi = navi)
    private var planSearchJob: Job? = null

    init {
        refreshRecommendAndHistory()
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

    fun updateDisplayName(name: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile.copy(
                    displayName = name.trim().ifEmpty { state.profile.displayName },
                ),
            )
        }
    }

    fun updateVehicleModel(model: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile.copy(
                    vehicleModel = model.trim().ifEmpty { state.profile.vehicleModel },
                ),
            )
        }
    }

    fun updateVehiclePlate(plate: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile.copy(
                    vehiclePlate = plate.trim().ifEmpty { state.profile.vehiclePlate },
                ),
            )
        }
    }

    /** 추천 탭에서 후보 코스를 오늘 루트로 적용 (Idle·리뷰 없을 때만). */
    fun applyCourseCandidate(courseId: String) {
        if (_uiState.value.phase !is TripPhase.Idle) {
            _uiState.update { it.copy(snackbar = "여정 중에는 코스를 바꿀 수 없어요. 계획실행에서 이어가세요.") }
            return
        }
        if (_uiState.value.review != null) {
            _uiState.update { it.copy(snackbar = "리뷰를 먼저 마쳐 주세요.") }
            return
        }
        val candidate = _uiState.value.courseCandidates.firstOrNull { it.courseId == courseId }
            ?: _uiState.value.courseOffer?.takeIf { it.courseId == courseId }
            ?: catalogCandidate(courseId)
            ?: return
        courseOfferDismissed = true
        applyCandidate(candidate)
    }

    private fun catalogCandidate(courseId: String): CourseCandidate? {
        val plan = courseCatalog.firstOrNull { it.id == courseId } ?: return null
        val selected = plan.selectedStops().ifEmpty { plan.stops }
        return CourseCandidate(
            courseId = plan.id,
            title = plan.title,
            stopPoiIds = selected.map { it.poiId },
            stops = selected,
            score = 0.0,
            positiveHitCount = 0,
            negativeHitCount = 0,
        )
    }

    fun toggleStop(stopId: String, selected: Boolean) {
        if (!DrivingEditLock.isEditingAllowed(_uiState.value.phase)) return
        if (_uiState.value.courseOffer != null) return
        if (!selected) {
            removePlanRouteStop(stopId)
            return
        }
        _uiState.update { it.copy(trip = it.trip.withStopSelection(stopId, true)) }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 계획 탭 — 지도 검색어 변경 (브이월드 우선, 폴백 카탈로그) */
    fun updatePlanSearchQuery(query: String) {
        if (!canEditPlan()) return
        _uiState.update { state ->
            state.copy(
                planMap = state.planMap.copy(
                    query = query,
                    searching = query.isNotBlank(),
                    searchSourceLabel = if (query.isBlank()) "" else "검색 중…",
                    searchResults = if (query.isBlank()) emptyList() else state.planMap.searchResults,
                ),
            )
        }
        planSearchJob?.cancel()
        if (query.isBlank()) return
        planSearchJob = scope.launch {
            delay(280)
            val near = _uiState.value.planMap.focus ?: MapSearchCatalog.home
            val outcome = withContext(Dispatchers.IO) {
                VWorldPlaceSearch.search(
                    query = query,
                    nearLat = near.latitude,
                    nearLng = near.longitude,
                )
            }
            _uiState.update { state ->
                if (state.planMap.query.trim() != query.trim()) return@update state
                state.copy(
                    planMap = state.planMap.copy(
                        searchResults = outcome.places,
                        searching = false,
                        searchSourceLabel = outcome.sourceLabel,
                    ),
                )
            }
        }
    }

    /**
     * 검색/목록에서 장소 선택 → 코스에 넣고
     * 「여기서 종료 / 다음 경로 추가」 확인 + 주변 맛집·볼거리 노출.
     */
    fun selectPlanAnchor(placeId: String) {
        if (!canEditPlan()) return
        val place = resolvePlace(placeId) ?: return
        val companionFamily = _uiState.value.planMap.companion == PlanCompanion.Family
        val buckets = MapSearchCatalog.nearbyBuckets(
            place,
            _uiState.value.family,
            companionIsFamily = companionFamily,
        )
        _uiState.update { state ->
            val map = state.planMap
            val stops = map.routeStops.toMutableList()
            when {
                stops.isEmpty() -> {
                    stops += MapSearchCatalog.home
                    stops += place
                }
                map.isOnRoute(placeId) -> Unit
                else -> {
                    // 새 후보를 도착으로 두고, 기존 도착은 경유로 밀림
                    if (stops.size == 1) stops += place else {
                        stops[stops.lastIndex] = place
                    }
                }
            }
            val planMap = map.copy(
                query = "",
                searchResults = emptyList(),
                searching = false,
                searchSourceLabel = "",
                focus = place,
                pendingPlace = place,
                nearbyFood = buckets.food,
                nearbySights = buckets.sights,
                nearbyRecommended = buckets.recommended,
                routeStops = stops,
                routeReady = false,
                routeSummary = null,
                weekendSaved = false,
            )
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = "${place.name}을(를) 코스에 넣었어요. 종료할지 더 추가할지 골라 주세요.",
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 코스 확정 — 여기서 종료하고 길찾기 */
    fun confirmPlanEnd() {
        if (!canEditPlan()) return
        _uiState.update { state ->
            state.copy(planMap = state.planMap.copy(pendingPlace = null))
        }
        findPlanRoute()
    }

    /** 다음 경로를 더 추가 — 주변 추천/검색 계속 */
    fun confirmPlanContinue() {
        if (!canEditPlan()) return
        _uiState.update { state ->
            val map = state.planMap
            state.copy(
                planMap = map.copy(pendingPlace = null),
                snackbar = "주변 맛집·볼거리를 추가하거나, 다시 검색해 다음 장소를 고르세요.",
            )
        }
    }

    /** 경로에 경유지 추가 (도착 직전) */
    fun addPlanRouteStop(placeId: String) {
        if (!canEditPlan()) return
        val place = resolvePlace(placeId) ?: return
        _uiState.update { state ->
            val map = state.planMap
            if (map.isOnRoute(placeId)) return@update state
            val stops = map.routeStops.toMutableList()
            when {
                stops.isEmpty() -> {
                    stops += MapSearchCatalog.home
                    stops += place
                }
                stops.size == 1 -> stops += place
                else -> stops.add(stops.lastIndex, place) // 도착 직전 경유
            }
            val planMap = withVisitFeasibility(
                map.copy(routeStops = stops, routeReady = false, routeSummary = null),
            )
            state.copy(planMap = planMap, trip = planMap.toTripPlan(state.trip))
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 경로에서 스톱 삭제 (출발·단독 도착은 유지) */
    fun removePlanRouteStop(placeId: String) {
        if (!canEditPlan()) return
        if (placeId == MapSearchCatalog.home.id) {
            _uiState.update { it.copy(snackbar = "출발지(집)는 삭제할 수 없어요.") }
            return
        }
        _uiState.update { state ->
            val map = state.planMap
            val stops = map.routeStops.filterNot { it.id == placeId }
            if (stops.size < 2 && map.focus != null) {
                val rebuilt = listOf(MapSearchCatalog.home, map.focus)
                val planMap = withVisitFeasibility(
                    map.copy(routeStops = rebuilt, routeReady = false, routeSummary = null),
                )
                return@update state.copy(
                    planMap = planMap,
                    trip = planMap.toTripPlan(state.trip),
                    snackbar = planMap.visitSummaryKo,
                )
            }
            val planMap = withVisitFeasibility(
                map.copy(routeStops = stops, routeReady = false, routeSummary = null),
            )
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = "경로에서 빼었어요. ${planMap.visitSummaryKo.orEmpty()}".trim(),
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 방문 불가 스톱을 대체 장소로 교체 */
    fun replacePlanRouteStop(fromPlaceId: String, toPlaceId: String) {
        if (!canEditPlan()) return
        if (fromPlaceId == MapSearchCatalog.home.id) return
        val replacement = resolvePlace(toPlaceId)
            ?: _uiState.value.planMap.visitChecks
                .flatMap { it.alternatives }
                .firstOrNull { it.id == toPlaceId }
            ?: return
        _uiState.update { state ->
            val map = state.planMap
            if (map.routeStops.none { it.id == fromPlaceId }) return@update state
            val stops = map.routeStops
                .map { if (it.id == fromPlaceId) replacement else it }
                .distinctBy { it.id }
            val focus = if (map.focus?.id == fromPlaceId) replacement else map.focus
            val planMap = withVisitFeasibility(
                map.copy(
                    routeStops = stops,
                    focus = focus,
                    routeReady = false,
                    routeSummary = null,
                    weekendSaved = false,
                ),
            )
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = "${replacement.name}(으)로 바꿨어요. ${planMap.visitSummaryKo.orEmpty()}".trim(),
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 길찾기 — 현재 수동 순서 그대로 요약 (거리 최적 재정렬 없음) */
    fun findPlanRoute() {
        if (!canEditPlan()) return
        val map = _uiState.value.planMap
        if (map.routeStops.size < 2) {
            _uiState.update { it.copy(snackbar = "출발과 도착을 먼저 정해 주세요.") }
            return
        }
        val via = (map.routeStops.size - 2).coerceAtLeast(0)
        val orderKm = estimateRouteKm(map.routeStops)
        val minutes = (orderKm * 2.2).toInt().coerceIn(18, 180) + via * 8
        val summary = "약 ${minutes}분 · ${"%.1f".format(orderKm)}km · 경유 ${via}곳 · 내가 정한 순서"
        _uiState.update { state ->
            val planMap = withVisitFeasibility(
                state.planMap.copy(routeReady = true, routeSummary = summary),
            )
            val issueHint = if (planMap.visitChecks.any { it.isIssue }) {
                " · ${planMap.visitSummaryKo}"
            } else {
                ""
            }
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = "경로를 찾았어요. $summary$issueHint",
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 출발 예정 일시 */
    fun setPlanDepartAt(epochMs: Long) {
        if (!canEditPlan()) return
        _uiState.update { state ->
            val planMap = withVisitFeasibility(
                state.planMap.copy(departAtEpochMs = epochMs),
            )
            state.copy(
                planMap = planMap,
                snackbar = "출발 ${VisitFeasibilityAnalyzer.formatDepartLabel(epochMs)} · ${planMap.visitSummaryKo.orEmpty()}".trim(),
            )
        }
    }

    /** 누구와/언제/출발일시 사전 세팅 확정 */
    fun confirmPlanPrefs() {
        if (!canEditPlan()) return
        _uiState.update { state ->
            val planMap = withVisitFeasibility(state.planMap.copy(prefsReady = true))
            state.copy(
                planMap = planMap,
                snackbar = "여행 설정을 저장했어요. ${planMap.prefsSummaryKo(state.family.familyName)}",
            )
        }
    }

    /** 세팅 팝업을 다시 열기 위해 확정 플래그만 해제 (값은 유지) */
    fun reopenPlanPrefs() {
        if (!canEditPlan()) return
        _uiState.update { state ->
            state.copy(planMap = state.planMap.copy(prefsReady = false))
        }
    }

    /** 바로 실행 / 주말 계획 */
    fun setPlanTiming(timing: PlanTiming) {
        if (!canEditPlan()) return
        _uiState.update { state ->
            val map = state.planMap
            val depart = when (timing) {
                PlanTiming.Now -> PlanMapUi.defaultDepartAtEpochMs()
                PlanTiming.Weekend -> weekendDepartSuggestion(map.departAtEpochMs)
            }
            val planMap = withVisitFeasibility(
                map.copy(
                    timing = timing,
                    weekendSaved = if (timing == PlanTiming.Now) false else map.weekendSaved,
                    departAtEpochMs = depart,
                ),
            )
            state.copy(
                planMap = planMap,
                snackbar = when (timing) {
                    PlanTiming.Now -> "지금 바로 실행할 코스로 설정했어요."
                    PlanTiming.Weekend -> "주말에 갈 코스로 담아둘게요. 출발 일시를 확인하세요."
                },
            )
        }
    }

    /** 등록 가족 / 혼자 / 연인·친구 — 주변 추천 기준에 반영 */
    fun setPlanCompanion(companion: PlanCompanion) {
        if (!canEditPlan()) return
        _uiState.update { state ->
            val map = state.planMap
            val focus = map.focus
            val refreshed = if (focus != null) {
                val buckets = MapSearchCatalog.nearbyBuckets(
                    focus,
                    state.family,
                    companionIsFamily = companion == PlanCompanion.Family,
                )
                map.copy(
                    companion = companion,
                    nearbyFood = buckets.food,
                    nearbySights = buckets.sights,
                    nearbyRecommended = buckets.recommended,
                )
            } else {
                map.copy(companion = companion)
            }
            val planMap = withVisitFeasibility(refreshed)
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = when (companion) {
                    PlanCompanion.Family -> "등록 가족과 함께 가는 코스로 맞출게요."
                    PlanCompanion.Solo -> "혼자 가는 코스로 맞출게요."
                    PlanCompanion.Friends -> "연인·친구와 가는 코스로 맞출게요."
                },
            )
        }
    }

    /**
     * 방문 순서 변경. 출발(집)은 고정, 그 뒤 스톱만 위/아래로 이동.
     * delta: -1 위로, +1 아래로.
     */
    fun movePlanRouteStop(placeId: String, delta: Int) {
        if (!canEditPlan()) return
        if (placeId == MapSearchCatalog.home.id) {
            _uiState.update { it.copy(snackbar = "출발지(집) 순서는 고정이에요.") }
            return
        }
        _uiState.update { state ->
            val map = state.planMap
            val stops = map.routeStops.toMutableList()
            val from = stops.indexOfFirst { it.id == placeId }
            if (from <= 0) return@update state // 0=집 또는 없음
            val to = (from + delta).coerceIn(1, stops.lastIndex)
            if (to == from) return@update state
            val item = stops.removeAt(from)
            stops.add(to, item)
            val planMap = withVisitFeasibility(
                map.copy(
                    routeStops = stops,
                    routeReady = false,
                    routeSummary = null,
                    weekendSaved = false,
                ),
            )
            state.copy(
                planMap = planMap,
                trip = planMap.toTripPlan(state.trip),
                snackbar = "순서를 바꿨어요. ${planMap.visitSummaryKo.orEmpty()}".trim(),
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 주말 계획으로 저장 (실행 탭으로 가지 않음) */
    fun saveWeekendPlan() {
        if (!canEditPlan()) return
        val map = _uiState.value.planMap
        if (map.routeStops.size < 2) {
            _uiState.update { it.copy(snackbar = "저장하려면 경로를 먼저 짜 주세요.") }
            return
        }
        if (!map.routeReady) findPlanRoute()
        _uiState.update { state ->
            val ready = state.planMap.copy(
                timing = PlanTiming.Weekend,
                weekendSaved = true,
                routeReady = true,
            )
            state.copy(
                planMap = ready,
                trip = ready.toTripPlan(state.trip),
                snackbar = "주말 계획으로 저장했어요. 실행 전에 순서를 다시 바꿀 수 있어요.",
            )
        }
        coordinator.bindTrip(_uiState.value.trip)
    }

    /** 주변 카드 토글 — 경로 추가/삭제 */
    fun togglePlanNearby(placeId: String, selected: Boolean) {
        if (selected) addPlanRouteStop(placeId) else removePlanRouteStop(placeId)
    }

    private fun withVisitFeasibility(map: PlanMapUi): PlanMapUi {
        if (map.routeStops.size < 2) {
            return map.copy(visitChecks = emptyList(), visitSummaryKo = null)
        }
        val pool = buildList {
            addAll(map.nearbyFood)
            addAll(map.nearbySights)
            addAll(map.nearbyRecommended)
            map.focus?.let { addAll(MapSearchCatalog.nearbyBuckets(it).all()) }
        }
        val report = VisitFeasibilityAnalyzer.evaluate(
            routeStops = map.routeStops,
            departAtEpochMs = map.departAtEpochMs,
            alternativePool = pool,
        )
        return map.copy(
            visitChecks = report.checks,
            visitSummaryKo = report.summaryKo,
        )
    }

    private fun weekendDepartSuggestion(current: Long): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = current }
        // 다가오는 토요일 10:00
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SATURDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 10)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 7)
        }
        return cal.timeInMillis
    }

    private fun estimateRouteKm(stops: List<MapPlace>): Double {
        if (stops.size < 2) return 0.0
        var sum = 0.0
        for (i in 0 until stops.lastIndex) {
            sum += haversineKm(stops[i], stops[i + 1])
        }
        return sum.coerceAtLeast(1.0)
    }

    private fun haversineKm(a: MapPlace, b: MapPlace): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = kotlin.math.sin(dLat / 2).pow(2) +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * kotlin.math.sin(dLon / 2).pow(2)
        return 2 * r * kotlin.math.asin(kotlin.math.min(1.0, kotlin.math.sqrt(h)))
    }

    private fun resolvePlace(placeId: String): MapPlace? {
        val map = _uiState.value.planMap
        return map.searchResults.firstOrNull { it.id == placeId }
            ?: MapSearchCatalog.searchablePlaces.firstOrNull { it.id == placeId }
            ?: MapSearchCatalog.frequentPlaces.firstOrNull { it.id == placeId }
            ?: map.nearbyFood.firstOrNull { it.id == placeId }
            ?: map.nearbySights.firstOrNull { it.id == placeId }
            ?: map.nearbyRecommended.firstOrNull { it.id == placeId }
            ?: map.routeStops.firstOrNull { it.id == placeId }
            ?: map.visitChecks.flatMap { it.alternatives }.firstOrNull { it.id == placeId }
            ?: map.focus?.let { MapSearchCatalog.nearbyOf(it).firstOrNull { p -> p.id == placeId } }
    }

    private fun canEditPlan(): Boolean {
        val state = _uiState.value
        if (!DrivingEditLock.isEditingAllowed(state.phase)) {
            _uiState.update { it.copy(snackbar = "여정 중에는 계획을 바꿀 수 없어요.") }
            return false
        }
        if (state.review != null) {
            _uiState.update { it.copy(snackbar = "리뷰를 먼저 마쳐 주세요.") }
            return false
        }
        return true
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

    /** 차내비·실행 화면 「다시 듣기」— 목적지 브리핑/다음 안내 멘트 재재생 */
    fun replayGuideSpeech() {
        val spoken = _uiState.value.lastSpoken
        if (spoken.isNotBlank()) speech.speak(spoken)
    }

    fun resetTrip() {
        val planMap = PlanMapUi.seededForMuseumTrip()
        val trip = planMap.toTripPlan(PresetTrips.weekendMuseumTrip)
        adaptVoice.reset()
        coordinator.reset(trip)
        _uiState.update {
            it.copy(
                trip = trip,
                planMap = planMap,
                review = null,
                courseAppliedConfirm = false,
                adaptSuggestion = null,
                snackbar = "일정을 초기화했습니다. 다시 시작할 수 있습니다.",
            )
        }
        refreshRecommendAndHistory()
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
        _uiState.update {
            it.copy(
                review = null,
                snackbar = "나중에 알림으로 다시 안내할게요",
            )
        }
        refreshRecommendAndHistory()
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
            it.copy(
                review = ReviewFlowActions.fromSession(session, selected),
                snackbar = "오늘 루트 완료 · 알림에서 별점·후기를 남겨 보세요",
            )
        }
        appContext?.let { ctx ->
            ReviewCompletionNotifier.notifyTripCompleted(
                context = ctx,
                tripTitle = trip.title,
                stopCount = selected.size,
            )
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
            refreshHistoryOnly()
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
        refreshRecommendAndHistory()
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

    private fun refreshRecommendAndHistory() {
        val candidates = CourseRecommender.recommend(reviewStore, courseCatalog)
        val sessions = reviewStore.loadSessions()
            .sortedByDescending { it.completedAtEpochMs ?: it.startedAtEpochMs }
        val completed = reviewStore.completedSessionCount()
        _uiState.update { state ->
            val offer = when {
                state.review != null || courseOfferDismissed -> null
                else -> candidates.firstOrNull()
            }
            state.copy(
                courseOffer = offer,
                courseCandidates = candidates,
                completedSessionCount = completed,
                pastSessions = sessions,
            )
        }
    }

    private fun refreshHistoryOnly() {
        val sessions = reviewStore.loadSessions()
            .sortedByDescending { it.completedAtEpochMs ?: it.startedAtEpochMs }
        _uiState.update {
            it.copy(
                pastSessions = sessions,
                completedSessionCount = reviewStore.completedSessionCount(),
            )
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
            refreshRecommendAndHistory()
        }
    }
}
