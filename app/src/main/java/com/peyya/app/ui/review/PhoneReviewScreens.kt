package com.peyya.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.review.ActiveReviewUi
import com.peyya.app.review.CourseCandidate
import com.peyya.app.review.ReviewUiStep
import com.peyya.app.ui.PhoneActionDock
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.components.StopState
import com.peyya.app.ui.components.StopTimelineRow
import com.peyya.app.ui.shortAddress
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.ui.theme.PeyyaSpace
import com.peyya.app.viewmodel.TripUiState

@Composable
fun PhoneReviewOrCourseOverlay(
    state: TripUiState,
    callbacks: ReviewFlowCallbacks,
    modifier: Modifier = Modifier,
) {
    when {
        state.courseAppliedConfirm -> PhoneCourseAppliedConfirm(
            onConfirm = callbacks.onAcknowledgeCourseApplied,
            modifier = modifier,
        )
        state.review != null -> when (state.review.step) {
            ReviewUiStep.Intro -> PhoneReviewIntro(
                review = state.review,
                onBegin = callbacks.onBeginReview,
                onSkip = callbacks.onSkipReviewSession,
                modifier = modifier,
            )
            ReviewUiStep.Rating -> PhoneStopRating(
                review = state.review,
                onStars = callbacks.onSetReviewStars,
                onComment = callbacks.onSetReviewComment,
                onCommit = callbacks.onCommitReviewStop,
                onSkipStop = callbacks.onSkipReviewStop,
                modifier = modifier,
            )
            ReviewUiStep.Submitted -> PhoneReviewSubmitted(
                onDone = callbacks.onAcknowledgeReviewSubmitted,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PhoneReviewIntro(
    review: ActiveReviewUi,
    onBegin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ReviewInviteNotificationCard(
                tripTitle = review.tripTitle,
                stopCount = review.stops.size,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "다녀온 장소",
                color = PeyyaInkDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            review.stops.forEachIndexed { index, stop ->
                StopTimelineRow(
                    order = index + 1,
                    name = stop.name,
                    caption = "${shortAddress(stop.address)} · 방문함",
                    state = StopState.Done,
                    isLast = index == review.stops.lastIndex,
                )
            }
        }
        PhoneActionDock {
            PrimaryCta(label = "별점·후기 작성", enabled = true, onClick = onBegin)
            SoftCta(label = "나중에", onClick = onSkip)
        }
    }
}

@Composable
private fun ReviewInviteNotificationCard(
    tripTitle: String,
    stopCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PeyyaSoft)
            .padding(PeyyaSpace.lg),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("알림 · 페이야", color = PeyyaBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            "오늘 루트가 끝났어요",
            color = PeyyaInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            letterSpacing = (-0.3).sp,
        )
        Text(
            tripTitle,
            color = PeyyaInkSub,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (stopCount > 0) {
                "${stopCount}곳을 다녀왔어요. 장소마다 별점과 한 줄 후기를 남겨 주세요."
            } else {
                "별점과 한 줄 후기를 남겨 주세요."
            },
            color = PeyyaInkSub,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun PhoneStopRating(
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                review.progressLabel,
                color = PeyyaInkDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stop.name,
                color = PeyyaInk,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${shortAddress(stop.address)} · 별점은 필수, 글은 선택",
                color = PeyyaInkSub,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(20.dp))
            StarRatingRow(stars = review.draftStars, onSelect = onStars)
            Spacer(Modifier.height(24.dp))
            Text(
                "한 줄 메모 (선택)",
                color = PeyyaInkDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            CommentField(
                value = review.draftComment,
                onValueChange = onComment,
                placeholder = "짧게 남겨 주세요",
            )
        }
        PhoneActionDock {
            PrimaryCta(
                label = primaryLabel,
                enabled = review.draftStars in 1..5,
                onClick = onCommit,
            )
            SecondaryCta(label = "이 장소 건너뛰기", onClick = onSkipStop)
        }
    }
}

@Composable
private fun PhoneReviewSubmitted(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        QuietHeroCard(
            kicker = "저장 완료",
            title = "리뷰를 남겼어요",
            body = "다음에 Idle에서 코스 추천에 반영될 수 있어요.",
        )
        Spacer(Modifier.weight(1f))
        PhoneActionDock {
            PrimaryCta(label = "확인", enabled = true, onClick = onDone)
        }
    }
}

@Composable
private fun PhoneCourseAppliedConfirm(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        QuietHeroCard(
            kicker = "코스 적용",
            title = "이 코스로 준비했어요",
            body = "경유지가 반영되었습니다. 운전 시작으로 바로 갈 수 있어요.",
        )
        Spacer(Modifier.weight(1f))
        PhoneActionDock {
            PrimaryCta(label = "확인", enabled = true, onClick = onConfirm)
        }
    }
}

@Composable
fun QuietHeroCard(
    kicker: String,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PeyyaSoft)
            .padding(PeyyaSpace.lg),
        verticalArrangement = Arrangement.spacedBy(PeyyaSpace.sm),
    ) {
        Text(kicker, color = PeyyaBrand, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            title,
            color = PeyyaInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 34.sp,
            letterSpacing = (-0.4).sp,
        )
        Text(body, color = PeyyaInkSub, fontSize = 15.sp, lineHeight = 22.sp)
    }
}

@Composable
fun CourseCard(candidate: CourseCandidate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, PeyyaLine, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("코스 추천", color = PeyyaBrand, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            candidate.title,
            color = PeyyaInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            "긍정 리뷰가 많은 장소 위주 · 부정 페널티 반영",
            color = PeyyaInkDim,
            fontSize = 13.sp,
        )
        candidate.stops.forEachIndexed { index, stop ->
            Text(
                "${index + 1}. ${stop.name}",
                color = PeyyaInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun SoftCta(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PeyyaSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = PeyyaInkSub, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CommentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = PeyyaInk, fontSize = 15.sp, lineHeight = 22.sp),
        cursorBrush = SolidColor(PeyyaBrand),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PeyyaLine, RoundedCornerShape(14.dp))
            .padding(14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, color = PeyyaInkDim, fontSize = 15.sp)
            }
            inner()
        },
    )
}
