package com.peyya.app.ui.review

import com.peyya.app.domain.TripPhase
import com.peyya.app.viewmodel.TripUiState

/** Plan 탭을 리뷰/적용확인 풀스크린으로 바꿀지. 코스 오퍼는 인라인·추천 탭으로만. */
fun TripUiState.showsReviewOrCourseUi(): Boolean =
    courseAppliedConfirm || review != null

/** Idle Plan 홈에 붙는 코스 soft card. */
fun TripUiState.showsInlineCourseCard(): Boolean =
    courseOffer != null && phase is TripPhase.Idle && review == null && !courseAppliedConfirm
