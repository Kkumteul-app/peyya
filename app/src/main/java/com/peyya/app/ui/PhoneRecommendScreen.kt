package com.peyya.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.CompanionType
import com.peyya.app.domain.PlaceReviewPost
import com.peyya.app.ui.media.PeyyaImage
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.ui.theme.PeyyaSpace
import com.peyya.app.viewmodel.TripUiState

/**
 * 추천 탭 — 방문 리뷰 피드 (사진 · 동행 · 별점).
 */
@Composable
fun PhoneRecommendScreen(
    state: TripUiState,
    contentPadding: PaddingValues,
) {
    val insets = phoneContentInsets(contentPadding)
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
                "다녀온 사람들의 사진과 별점으로 다음 장소를 골라 보세요.",
                color = PeyyaInkSub,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        items(state.communityReviews, key = { it.id }) { post ->
            ReviewFeedCard(post)
        }

        item { Spacer(Modifier.height(PeyyaSpace.xs)) }
    }
}

@Composable
private fun ReviewFeedCard(post: PlaceReviewPost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PeyyaSoft),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            PeyyaImage(
                model = post.photoAsset,
                contentDescription = post.placeName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Text(
                post.visitedLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PeyyaBrand.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        post.authorName.take(1),
                        color = PeyyaBrand,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, color = PeyyaInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        companionLabel(post.companion),
                        color = PeyyaInkDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                StarRow(stars = post.stars)
            }
            Text(post.placeName, color = PeyyaInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(post.placeMeta, color = PeyyaInkDim, fontSize = 13.sp)
            Text(post.comment, color = PeyyaInkSub, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = if (index < stars) Color(0xFFE6A700) else PeyyaInkDim.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun companionLabel(type: CompanionType): String = when (type) {
    CompanionType.ALONE -> "혼자 방문"
    CompanionType.COUPLE -> "연인과 함께"
    CompanionType.FAMILY -> "가족과 함께"
}
