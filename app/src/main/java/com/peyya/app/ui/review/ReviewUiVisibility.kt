package com.peyya.app.ui.review

import com.peyya.app.domain.TripPhase
import com.peyya.app.viewmodel.TripUiState

fun TripUiState.showsReviewOrCourseUi(): Boolean =
    courseAppliedConfirm ||
        review != null ||
        (courseOffer != null && phase is TripPhase.Idle)
