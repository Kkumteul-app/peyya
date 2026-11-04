package com.peyya.app.review

import com.peyya.app.domain.TripPlan

/**
 * 로컬 코스 추천 엔진.
 *
 * - M=3: 완료(Completed) 세션 수 ≥ 3
 * - N=5: 점수에 넣는 각 POI 누적 리뷰 ≥ 5 (미달 POI는 제외)
 * - 긍정 가중 / 부정 페널티 / Neutral은 0
 * - M 미달 또는 유효 후보 없으면 empty (가짜 후보 금지)
 */
object CourseRecommender {
    const val M: Int = 3
    const val N: Int = 5
    const val POSITIVE_WEIGHT: Double = 1.0
    const val NEGATIVE_PENALTY: Double = -1.0
    const val NEUTRAL_WEIGHT: Double = 0.0

    fun recommend(store: ReviewStore, catalog: List<TripPlan>): List<CourseCandidate> {
        if (store.completedSessionCount() < M) return emptyList()

        val sentimentsByPoi = completedSentimentsByPoi(store)
        return catalog
            .mapNotNull { plan -> scorePlan(plan, store, sentimentsByPoi) }
            .sortedByDescending { it.score }
    }

    private fun completedSentimentsByPoi(store: ReviewStore): Map<String, List<SentimentLabel>> {
        return store.loadSessions()
            .asSequence()
            .filter { it.status == TripReviewSessionStatus.Completed }
            .flatMap { it.stopReviews.asSequence() }
            .groupBy({ it.poiId }, { it.sentiment.label })
    }

    private fun scorePlan(
        plan: TripPlan,
        store: ReviewStore,
        sentimentsByPoi: Map<String, List<SentimentLabel>>,
    ): CourseCandidate? {
        val selected = plan.selectedStops().ifEmpty { plan.stops }
        val poiIds = selected.map { it.poiId }.distinct()

        var score = 0.0
        var positiveHits = 0
        var negativeHits = 0
        var eligiblePois = 0

        for (poiId in poiIds) {
            if (store.reviewCountForPoi(poiId) < N) continue
            eligiblePois += 1
            val labels = sentimentsByPoi[poiId].orEmpty()
            for (label in labels) {
                when (label) {
                    SentimentLabel.Positive -> {
                        score += POSITIVE_WEIGHT
                        positiveHits += 1
                    }
                    SentimentLabel.Negative -> {
                        score += NEGATIVE_PENALTY
                        negativeHits += 1
                    }
                    SentimentLabel.Neutral -> {
                        score += NEUTRAL_WEIGHT
                    }
                }
            }
        }

        if (eligiblePois == 0) return null

        return CourseCandidate(
            courseId = plan.id,
            title = plan.title,
            stopPoiIds = selected.map { it.poiId },
            stops = selected,
            score = score,
            positiveHitCount = positiveHits,
            negativeHitCount = negativeHits,
        )
    }
}
