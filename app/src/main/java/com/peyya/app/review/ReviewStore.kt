package com.peyya.app.review

/**
 * 로컬 리뷰 세션 저장소.
 *
 * GPS lat/lng·궤적은 스키마에 두지 않는다 — [StopReview]는 poiId/이름 스냅샷만 보관.
 */
interface ReviewStore {
    fun save(session: TripReviewSession)

    fun loadSessions(): List<TripReviewSession>

    /** 검수·데모용 시드. 이미 로드됐으면 no-op. */
    fun loadSeedIfNeeded()

    /** status=Completed 만 집계 (Skipped / InProgress 제외). */
    fun completedSessionCount(): Int

    fun reviewCountForPoi(poiId: String): Int
}

/**
 * 프로세스 재시작 시뮬레이션·단위 테스트용 인메모리 구현.
 */
class InMemoryReviewStore private constructor(
    initial: List<TripReviewSession>,
    private var seedLoaded: Boolean,
) : ReviewStore {

    private val sessions = initial.toMutableList()

    constructor() : this(emptyList(), seedLoaded = false)

    override fun save(session: TripReviewSession) {
        val index = sessions.indexOfFirst { it.sessionId == session.sessionId }
        if (index >= 0) {
            sessions[index] = session
        } else {
            sessions.add(session)
        }
    }

    override fun loadSessions(): List<TripReviewSession> = sessions.toList()

    override fun loadSeedIfNeeded() {
        if (seedLoaded) return
        ReviewSeed.sessions.forEach { save(it) }
        seedLoaded = true
    }

    override fun completedSessionCount(): Int =
        sessions.count { it.status == TripReviewSessionStatus.Completed }

    override fun reviewCountForPoi(poiId: String): Int =
        sessions
            .asSequence()
            .filter { it.status == TripReviewSessionStatus.Completed }
            .flatMap { it.stopReviews.asSequence() }
            .count { it.poiId == poiId }

    companion object {
        /** 재시작 복원: 직렬화 결과로 새 인스턴스를 채운다. */
        fun fromSessions(sessions: List<TripReviewSession>): InMemoryReviewStore =
            InMemoryReviewStore(sessions, seedLoaded = false)
    }
}

/**
 * 완료 루트 ≥3 · POI(박물관·맛집) 리뷰 ≥5 를 만족하는 시드.
 * 좌표는 포함하지 않는다.
 */
internal object ReviewSeed {
    private const val MUSEUM = "peyya-demo-museum"
    private const val RESTAURANT = "peyya-demo-restaurant"
    private const val BASE_MS = 1_700_100_000_000L

    val sessions: List<TripReviewSession> = (0 until 5).map { index ->
        TripReviewSession(
            sessionId = "seed-session-$index",
            tripId = "seed-trip-$index",
            tripTitle = "시드 루트 ${index + 1}",
            status = TripReviewSessionStatus.Completed,
            stopReviews = listOf(
                stopReview(
                    index = index,
                    stopId = "museum",
                    poiId = MUSEUM,
                    stopName = "국립중앙박물관",
                    label = if (index == 4) SentimentLabel.Neutral else SentimentLabel.Positive,
                    stars = if (index == 4) 3 else 5,
                ),
                stopReview(
                    index = index,
                    stopId = "restaurant",
                    poiId = RESTAURANT,
                    stopName = "용산 한식 담소",
                    label = SentimentLabel.Positive,
                    stars = 4,
                ),
            ),
            startedAtEpochMs = BASE_MS + index * 1_000L,
            completedAtEpochMs = BASE_MS + index * 1_000L + 500L,
        )
    }

    private fun stopReview(
        index: Int,
        stopId: String,
        poiId: String,
        stopName: String,
        label: SentimentLabel,
        stars: Int,
    ): StopReview = StopReview(
        stopId = "$stopId-seed-$index",
        poiId = poiId,
        stopName = stopName,
        ratingStars = stars,
        comment = "",
        sentiment = Sentiment(label = label, source = SentimentSource.Stars),
        reviewedAtEpochMs = BASE_MS + index * 1_000L + 100L,
    )
}
