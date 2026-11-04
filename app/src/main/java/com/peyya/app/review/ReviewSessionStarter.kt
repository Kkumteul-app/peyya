package com.peyya.app.review

import java.util.UUID

/**
 * Completed 콜백에서 InProgress [TripReviewSession]을 만들고 저장한다.
 * Coordinator 안에서 UI를 열지 않는다 — 호출측이 UI 플래그를 켠다.
 */
class ReviewSessionStarter(
    private val store: ReviewStore,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    fun start(tripId: String, tripTitle: String): TripReviewSession {
        val session = TripReviewSession(
            sessionId = idFactory(),
            tripId = tripId,
            tripTitle = tripTitle,
            status = TripReviewSessionStatus.InProgress,
            stopReviews = emptyList(),
            startedAtEpochMs = clock(),
            completedAtEpochMs = null,
        )
        store.save(session)
        return session
    }
}
