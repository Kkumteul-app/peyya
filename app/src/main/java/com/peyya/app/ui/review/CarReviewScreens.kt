package com.peyya.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.TripPhase
import com.peyya.app.review.ActiveReviewUi
import com.peyya.app.review.CourseCandidate
import com.peyya.app.review.ReviewUiStep
import com.peyya.app.ui.components.CarOutlinedCta
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarCard
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.viewmodel.TripUiState

/** 차량 우측 패널용 리뷰·코스 오버레이. */
@Composable
fun CarReviewOrCoursePane(
    state: TripUiState,
    callbacks: ReviewFlowCallbacks,
    modifier: Modifier = Modifier,
) {
    when {
        state.courseAppliedConfirm -> {
            LaunchedEffect(Unit) { callbacks.onAcknowledgeCourseApplied() }
        }
        state.review != null -> when (state.review.step) {
            ReviewUiStep.Intro -> CarReviewIntro(
                onBegin = callbacks.onBeginReview,
                onSkip = callbacks.onSkipReviewSession,
                modifier = modifier,
            )
            ReviewUiStep.Rating -> CarStopRating(
                review = state.review,
                onStars = callbacks.onSetReviewStars,
                onComment = callbacks.onSetReviewComment,
                onCommit = callbacks.onCommitReviewStop,
                onSkipStop = callbacks.onSkipReviewStop,
                modifier = modifier,
            )
            ReviewUiStep.Submitted -> {
                LaunchedEffect(state.review.sessionId) {
                    callbacks.onAcknowledgeReviewSubmitted()
                }
            }
        }
        state.courseOffer != null && state.phase is TripPhase.Idle -> CarCourseRecommend(
            candidate = state.courseOffer,
            onApply = callbacks.onApplyCourse,
            onDismiss = callbacks.onDismissCourse,
            modifier = modifier,
        )
    }
}

@Composable
private fun CarReviewIntro(
    onBegin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "알림 · 페이야",
            color = CarBrandLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "오늘 루트가\n끝났어요",
            color = CarText,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 48.sp,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "알림으로도 안내했어요. 방문한 장소마다 별점과 짧은 글을 남겨 주세요.",
            color = CarTextSub,
            fontSize = 20.sp,
            lineHeight = 28.sp,
        )
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryCta(
                label = "별점·후기 작성",
                enabled = true,
                onClick = onBegin,
                modifier = Modifier.weight(2f),
                height = 84.dp,
                corner = 20.dp,
                textSize = 26.sp,
                textWeight = FontWeight.ExtraBold,
            )
            CarOutlinedCta(
                label = "나중에",
                onClick = onSkip,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CarStopRating(
    review: ActiveReviewUi,
    onStars: (Int) -> Unit,
    onComment: (String) -> Unit,
    onCommit: () -> Unit,
    onSkipStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stop = review.currentStop ?: return
    val primaryLabel = if (review.isLastStop) "완료" else "다음 장소"
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            review.progressLabel,
            color = CarBrandLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stop.name,
            color = CarText,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 48.sp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "별점은 필수입니다. 글은 선택이에요.",
            color = CarTextSub,
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(28.dp))
        StarRatingRow(
            stars = review.draftStars,
            onSelect = onStars,
            starSize = 48.sp,
            touchSize = 64.dp,
            activeColor = PeyyaBrand,
            inactiveColor = CarLineStrong,
        )
        Spacer(Modifier.height(24.dp))
        CarCommentField(value = review.draftComment, onValueChange = onComment)
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryCta(
                label = primaryLabel,
                enabled = review.draftStars in 1..5,
                onClick = onCommit,
                modifier = Modifier.weight(2f),
                height = 84.dp,
                corner = 20.dp,
                textSize = 26.sp,
                textWeight = FontWeight.ExtraBold,
            )
            CarOutlinedCta(
                label = "건너뛰기",
                onClick = onSkipStop,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CarCourseRecommend(
    candidate: CourseCandidate,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "추천 가능 · M·N 충족",
            color = CarBrandLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "이 코스로\n갈까요?",
            color = CarText,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 48.sp,
        )
        Spacer(Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CarCard)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("코스 추천", color = CarBrandLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                candidate.title,
                color = CarText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text("긍정 리뷰가 많은 장소 위주", color = CarDim, fontSize = 18.sp)
            candidate.stops.forEachIndexed { index, stop ->
                Text(
                    "${index + 1}. ${stop.name}",
                    color = CarText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryCta(
                label = "이 코스로",
                enabled = true,
                onClick = onApply,
                modifier = Modifier.weight(2f),
                height = 84.dp,
                corner = 20.dp,
                textSize = 26.sp,
                textWeight = FontWeight.ExtraBold,
            )
            CarOutlinedCta(
                label = "닫기",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CarCommentField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = CarText, fontSize = 20.sp, lineHeight = 28.sp),
        cursorBrush = SolidColor(PeyyaBrand),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CarLineStrong, RoundedCornerShape(20.dp))
            .padding(20.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text("한 줄 메모 (선택)", color = CarDim, fontSize = 20.sp)
            }
            inner()
        },
    )
}
