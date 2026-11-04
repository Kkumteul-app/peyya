package com.peyya.app.review

import com.peyya.app.domain.Stop

/**
 * 리뷰 UI 상태 전이 — I/O 없는 순수 함수.
 */
object ReviewFlowActions {
    fun fromSession(
        session: TripReviewSession,
        selectedStops: List<Stop>,
    ): ActiveReviewUi = ActiveReviewUi(
        sessionId = session.sessionId,
        tripId = session.tripId,
        tripTitle = session.tripTitle,
        stops = selectedStops,
        step = ReviewUiStep.Intro,
    )

    fun beginRating(ui: ActiveReviewUi): ActiveReviewUi =
        ui.copy(step = ReviewUiStep.Rating, stopIndex = 0, draftStars = 0, draftComment = "")

    fun setStars(ui: ActiveReviewUi, stars: Int): ActiveReviewUi {
        require(stars in 1..5)
        return ui.copy(draftStars = stars)
    }

    fun setComment(ui: ActiveReviewUi, comment: String): ActiveReviewUi =
        ui.copy(draftComment = comment)

    /**
     * 현재 스톱을 확정하고 다음으로 이동.
     * 마지막이면 [ReviewUiStep.Submitted] (폰) — 호출측이 Car면 Idle로 닫을 수 있다.
     */
    fun commitCurrentStop(
        ui: ActiveReviewUi,
        nowEpochMs: Long,
        carModeSkipsSubmitted: Boolean = false,
    ): CommitResult {
        if (ui.step != ReviewUiStep.Rating) return CommitResult.Ignored
        if (ui.draftStars !in 1..5) return CommitResult.NeedsStars
        val stop = ui.currentStop ?: return CommitResult.Ignored
        val review = buildStopReview(stop, ui.draftStars, ui.draftComment, nowEpochMs)
        val collected = ui.collected + review
        return if (ui.isLastStop) {
            val next = if (carModeSkipsSubmitted) {
                null
            } else {
                ui.copy(
                    collected = collected,
                    step = ReviewUiStep.Submitted,
                    draftStars = 0,
                    draftComment = "",
                )
            }
            CommitResult.Finished(collected = collected, nextUi = next)
        } else {
            CommitResult.Advanced(
                ui.copy(
                    collected = collected,
                    stopIndex = ui.stopIndex + 1,
                    draftStars = 0,
                    draftComment = "",
                ),
            )
        }
    }

    /** 현재 스톱만 건너뛰고 다음으로. 리뷰 목록에는 넣지 않는다. */
    fun skipCurrentStop(
        ui: ActiveReviewUi,
        carModeSkipsSubmitted: Boolean = false,
    ): CommitResult {
        if (ui.step != ReviewUiStep.Rating) return CommitResult.Ignored
        return if (ui.isLastStop) {
            val next = if (carModeSkipsSubmitted) {
                null
            } else {
                ui.copy(step = ReviewUiStep.Submitted, draftStars = 0, draftComment = "")
            }
            CommitResult.Finished(collected = ui.collected, nextUi = next)
        } else {
            CommitResult.Advanced(
                ui.copy(
                    stopIndex = ui.stopIndex + 1,
                    draftStars = 0,
                    draftComment = "",
                ),
            )
        }
    }

    fun toCompletedSession(
        ui: ActiveReviewUi,
        startedAtEpochMs: Long,
        completedAtEpochMs: Long,
    ): TripReviewSession = TripReviewSession(
        sessionId = ui.sessionId,
        tripId = ui.tripId,
        tripTitle = ui.tripTitle,
        status = TripReviewSessionStatus.Completed,
        stopReviews = ui.collected,
        startedAtEpochMs = startedAtEpochMs,
        completedAtEpochMs = completedAtEpochMs,
    )

    fun toSkippedSession(
        ui: ActiveReviewUi,
        startedAtEpochMs: Long,
        completedAtEpochMs: Long,
    ): TripReviewSession = TripReviewSession(
        sessionId = ui.sessionId,
        tripId = ui.tripId,
        tripTitle = ui.tripTitle,
        status = TripReviewSessionStatus.Skipped,
        stopReviews = emptyList(),
        startedAtEpochMs = startedAtEpochMs,
        completedAtEpochMs = completedAtEpochMs,
    )

    fun buildStopReview(
        stop: Stop,
        stars: Int,
        comment: String,
        nowEpochMs: Long,
    ): StopReview = StopReview(
        stopId = stop.id,
        poiId = stop.poiId,
        stopName = stop.name,
        ratingStars = stars,
        comment = comment,
        sentiment = SentimentLabeler.label(stars, comment),
        reviewedAtEpochMs = nowEpochMs,
    )

    sealed class CommitResult {
        data object Ignored : CommitResult()
        data object NeedsStars : CommitResult()
        data class Advanced(val ui: ActiveReviewUi) : CommitResult()
        data class Finished(
            val collected: List<StopReview>,
            val nextUi: ActiveReviewUi?,
        ) : CommitResult()
    }
}
