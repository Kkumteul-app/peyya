package com.peyya.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.data.DemoContent
import com.peyya.app.data.PresetTrips
import com.peyya.app.data.TripThemeAnalyzer
import com.peyya.app.domain.Stop
import com.peyya.app.domain.TripPlan
import com.peyya.app.review.CourseCandidate
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.media.PeyyaImage
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.ui.theme.PeyyaSpace
import com.peyya.app.viewmodel.PlanCompanion
import com.peyya.app.viewmodel.TripUiState

/**
 * 추천 탭 — 장소가 아니라 **여행 코스**.
 * 실행의 「오늘의 계획」과 같은 한 문장 테마를 카드 헤드라인으로 쓴다.
 */
@Composable
fun PhoneRecommendScreen(
    state: TripUiState,
    contentPadding: PaddingValues,
    onApplyCourse: (String) -> Unit = {},
) {
    val insets = phoneContentInsets(contentPadding)
    val companion = state.planMap.companion
    val courses = remember(state.courseCandidates, companion) {
        val scored = state.courseCandidates
        if (scored.isNotEmpty()) {
            scored
        } else {
            // M·N 미달 시에도 시연용으로 카탈로그 코스를 테마와 함께 보여 준다.
            PresetTrips.courseCatalog.map { it.toSeedCandidate() }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PeyyaCanvas),
        contentPadding = insets,
        verticalArrangement = Arrangement.spacedBy(PeyyaSpace.md),
    ) {
        item {
            Text("추천", color = PeyyaInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "장소가 아니라 여행 코스를 고릅니다. 실행의 「오늘의 계획」한 문장과 맞는 동선이에요.",
                color = PeyyaInkSub,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        items(courses, key = { it.courseId }) { candidate ->
            val theme = TripThemeAnalyzer.themeSentence(candidate.stops, companion)
            CourseRecommendCard(
                candidate = candidate,
                themeSentence = theme,
                isSeed = state.courseCandidates.none { it.courseId == candidate.courseId },
                onApply = { onApplyCourse(candidate.courseId) },
            )
        }

        item { Spacer(Modifier.height(PeyyaSpace.xs)) }
    }
}

@Composable
private fun CourseRecommendCard(
    candidate: CourseCandidate,
    themeSentence: String,
    isSeed: Boolean,
    onApply: () -> Unit,
) {
    val cover = coverForStops(candidate.stops)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, PeyyaLine, RoundedCornerShape(20.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            PeyyaImage(
                model = cover,
                contentDescription = candidate.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        ),
                    ),
            )
            Text(
                if (isSeed) "시연 코스" else "리뷰 기반",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("코스 테마", color = PeyyaBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                themeSentence,
                color = PeyyaInk,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp,
            )
            candidate.stops.forEachIndexed { index, stop ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(PeyyaSoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = PeyyaInkDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Text(stop.name, color = PeyyaInkSub, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(candidate.title, color = PeyyaInkDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (!isSeed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFE6A700),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            }
            PrimaryCta(label = "이 코스로 계획", enabled = true, onClick = onApply)
        }
    }
}

private fun coverForStops(stops: List<Stop>): String {
    val first = stops.firstOrNull() ?: return DemoContent.assetPath("road.jpg")
    return DemoContent.placePhotoByStopId[first.id]
        ?: DemoContent.assetPath("museum.jpg")
}

private fun TripPlan.toSeedCandidate(): CourseCandidate {
    val selected = selectedStops().ifEmpty { stops }
    return CourseCandidate(
        courseId = id,
        title = title,
        stopPoiIds = selected.map { it.poiId },
        stops = selected,
        score = 0.0,
        positiveHitCount = 0,
        negativeHitCount = 0,
    )
}
