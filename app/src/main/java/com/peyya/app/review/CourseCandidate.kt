package com.peyya.app.review

import com.peyya.app.domain.Stop

/**
 * Idle 코스 추천 후보. 점수는 [CourseRecommender]가 긍정 가중·부정 페널티로 산출한다.
 */
data class CourseCandidate(
    val courseId: String,
    val title: String,
    val stopPoiIds: List<String>,
    val stops: List<Stop>,
    val score: Double,
    val positiveHitCount: Int,
    val negativeHitCount: Int,
)
