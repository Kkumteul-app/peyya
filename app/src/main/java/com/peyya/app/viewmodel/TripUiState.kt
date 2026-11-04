package com.peyya.app.viewmodel

import com.peyya.app.adapt.AdaptSuggestion
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.TripPhase
import com.peyya.app.domain.TripPlan
import com.peyya.app.progress.ArrivalPath
import com.peyya.app.review.ActiveReviewUi
import com.peyya.app.review.CourseCandidate
import java.time.LocalDate

data class TripUiState(
    val family: FamilyProfile,
    val trip: TripPlan,
    val phase: TripPhase = TripPhase.Idle,
    val mockMode: Boolean = true,
    val notice: String? = null,
    val lastSpoken: String = "",
    val snackbar: String? = null,
    /** 시동 ON + Idle 조건 충족 시 리마인드 3선택 노출 */
    val ignitionPromptVisible: Boolean = false,
    /** GPS 체류 후 내비 미확정 — 사용자 「도착 완료」확인 */
    val arrivalConfirmVisible: Boolean = false,
    val snoozeUntilEpochMs: Long? = null,
    val skipTodayDate: LocalDate? = null,
    val lastArrivalPath: ArrivalPath? = null,
    /** Completed 후 「오늘 하루는 어땠나요?」세션. null이면 비활성 */
    val review: ActiveReviewUi? = null,
    /** Idle에서 M·N 충족 시 상위 코스 후보 1개. null이면 카드 숨김 */
    val courseOffer: CourseCandidate? = null,
    /** 폰 C2 — 코스 적용 확인 */
    val courseAppliedConfirm: Boolean = false,
    /** 도착·대기 맥락 혼잡/우회/주차 제안. null이면 숨김 */
    val adaptSuggestion: AdaptSuggestion? = null,
)
