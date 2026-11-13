package com.peyya.app.ui.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.CommunityRoute
import com.peyya.app.ui.components.AssetPhoto
import com.peyya.app.ui.components.BackHeader
import com.peyya.app.ui.components.CourseRouteBoard
import com.peyya.app.ui.components.PhotoHeroBanner
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.RoutePhotoRow
import com.peyya.app.ui.components.ScreenScaffold
import com.peyya.app.ui.components.SectionTitle
import com.peyya.app.ui.components.SoftCard
import com.peyya.app.ui.theme.PeyyaBrand
import java.util.Calendar

private enum class RecommendMode { Region, People }

private data class PersonReviewItem(
    val routeId: String,
    val author: String,
    val routeTitle: String,
    val region: String,
    val body: String,
    val stars: Int,
    val isAuthorReview: Boolean,
    val imageModel: String?,
)

private val demoPhotoPool = listOf(
    PeyyaImages.MUSEUM,
    PeyyaImages.SEONGSU,
    PeyyaImages.FOOD,
    PeyyaImages.CAFE,
    PeyyaImages.PARK,
    PeyyaImages.BRUNCH,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    contentPadding: PaddingValues,
    routes: List<CommunityRoute>,
    onLike: (String) -> Unit,
    onComment: (String, String, String?) -> Unit,
    onAdopt: (String, Long?) -> Unit,
) {
    var mode by remember { mutableStateOf(RecommendMode.Region) }
    var region by remember { mutableStateOf("전체") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val regions = listOf("전체") + routes.map { it.region }.distinct()
    val filtered = if (region == "전체") routes else routes.filter { it.region == region }
    val detail = routes.firstOrNull { it.id == selectedId }
    val peopleReviews = remember(routes) {
        routes.flatMap { route ->
            val authorCard = PersonReviewItem(
                routeId = route.id,
                author = route.authorLabel,
                routeTitle = route.title,
                region = route.region,
                body = route.satisfactionNote,
                stars = ((route.likes % 2) + 4).coerceIn(4, 5),
                isAuthorReview = true,
                imageModel = route.reviewImageModel,
            )
            val commentCards = route.comments.map { c ->
                PersonReviewItem(
                    routeId = route.id,
                    author = c.authorLabel,
                    routeTitle = route.title,
                    region = route.region,
                    body = c.body,
                    stars = 4,
                    isAuthorReview = false,
                    imageModel = c.imageModel,
                )
            }
            listOf(authorCard) + commentCards
        }
    }

    ScreenScaffold(contentPadding) {
        if (detail != null) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Detail(
                    route = detail,
                    onBack = { selectedId = null },
                    onLike = { onLike(detail.id) },
                    onComment = { body, img -> onComment(detail.id, body, img) },
                    onAdopt = { at -> onAdopt(detail.id, at) },
                )
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("추천", style = MaterialTheme.typography.titleLarge, color = PeyyaBrand)
                Text(
                    "지역 코스와 실제 이용 리뷰를 살펴보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == RecommendMode.Region,
                        onClick = { mode = RecommendMode.Region },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("지역별") }
                    SegmentedButton(
                        selected = mode == RecommendMode.People,
                        onClick = { mode = RecommendMode.People },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("사람별") }
                }
                Spacer(Modifier.height(14.dp))
                when (mode) {
                    RecommendMode.Region -> {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            regions.forEach { r ->
                                FilterChip(
                                    selected = region == r,
                                    onClick = { region = r },
                                    label = { Text(r) },
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SectionTitle("Recommended")
                        Spacer(Modifier.height(8.dp))
                        filtered.forEach { route ->
                            RoutePhotoRow(
                                title = route.title,
                                imageModel = PeyyaImages.forRegion(route.region),
                                meta = "${route.region} · 공감 ${route.likes} · ${route.satisfactionNote}",
                                actionLabel = "자세히",
                                onAction = { selectedId = route.id },
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    RecommendMode.People -> {
                        SectionTitle("체험 리뷰")
                        Spacer(Modifier.height(8.dp))
                        peopleReviews.forEach { item ->
                            PersonReviewCard(item) { selectedId = item.routeId }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonReviewCard(
    item: PersonReviewItem,
    onOpen: () -> Unit,
) {
    SoftCard(Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                color = PeyyaBrand.copy(alpha = 0.12f),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        item.author.take(1),
                        color = PeyyaBrand,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.author, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.region} · ${item.routeTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Row {
                repeat(item.stars) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = PeyyaBrand,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        item.imageModel?.let { img ->
            Spacer(Modifier.height(10.dp))
            AssetPhoto(
                img,
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp)),
                item.routeTitle,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(item.body, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            if (item.isAuthorReview) "코스 작성자 후기 · 탭하여 상세" else "이용자 댓글 · 탭하여 상세",
            style = MaterialTheme.typography.labelSmall,
            color = PeyyaBrand,
        )
    }
}

@Composable
private fun Detail(
    route: CommunityRoute,
    onBack: () -> Unit,
    onLike: () -> Unit,
    onComment: (String, String?) -> Unit,
    onAdopt: (Long?) -> Unit,
) {
    var comment by remember { mutableStateOf("") }
    var attached by remember { mutableStateOf<String?>(null) }
    var photoIdx by remember { mutableIntStateOf(0) }
    BackHeader(title = "루트 상세", onBack = onBack, backLabel = "목록으로")
    PhotoHeroBanner(
        imageModel = route.reviewImageModel ?: PeyyaImages.forRegion(route.region),
        title = route.title,
        subtitle = "${route.authorLabel} · ${route.region}",
        height = 140,
    )
    Spacer(Modifier.height(12.dp))
    CourseRouteBoard(
        title = route.title,
        subtitle = "${route.authorLabel} 체험 코스",
        stops = route.stops,
        heightDp = 220,
    )
    Spacer(Modifier.height(12.dp))
    SoftCard {
        Text(route.satisfactionNote)
        Spacer(Modifier.height(8.dp))
        route.stops.forEachIndexed { i, stop ->
            Text("${i + 1}. ${stop.name}\n   ${stop.address}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = onLike,
            label = { Text(if (route.likedByMe) "공감 ${route.likes} ✓" else "공감 ${route.likes}") },
        )
    }
    Spacer(Modifier.height(12.dp))
    PrimaryCta("내 루트로 담기 (예정)", true) {
        val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 4) }
        onAdopt(cal.timeInMillis)
    }
    Spacer(Modifier.height(8.dp))
    PrimaryCta("초안으로만 담기", true) { onAdopt(null) }
    Spacer(Modifier.height(16.dp))
    SectionTitle("댓글 · 체험 사진")
    Spacer(Modifier.height(8.dp))
    route.comments.forEach { c ->
        SoftCard(Modifier.padding(bottom = 8.dp)) {
            Text(c.authorLabel, style = MaterialTheme.typography.labelLarge)
            Text(c.body, style = MaterialTheme.typography.bodyMedium)
            c.imageModel?.let { img ->
                Spacer(Modifier.height(8.dp))
                AssetPhoto(
                    img,
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    "체험 사진",
                )
            }
        }
    }
    OutlinedTextField(
        value = comment,
        onValueChange = { comment = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("댓글 남기기") },
        shape = RoundedCornerShape(16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            onClick = {
                photoIdx = (photoIdx + 1) % demoPhotoPool.size
                attached = demoPhotoPool[photoIdx]
            },
        ) {
            Icon(Icons.Rounded.AddAPhoto, contentDescription = null, tint = PeyyaBrand)
            Spacer(Modifier.size(6.dp))
            Text(if (attached == null) "사진 첨부" else "사진 바꾸기", color = PeyyaBrand)
        }
        if (attached != null) {
            TextButton(onClick = { attached = null }) { Text("제거") }
        }
    }
    attached?.let { img ->
        AssetPhoto(
            img,
            Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp)),
            "첨부 미리보기",
        )
        Spacer(Modifier.height(8.dp))
    }
    PrimaryCta("댓글 등록", comment.isNotBlank()) {
        onComment(comment, attached)
        comment = ""
        attached = null
    }
}
