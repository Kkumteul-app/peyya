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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.data.DemoContent
import com.peyya.app.review.TripReviewSession
import com.peyya.app.review.TripReviewSessionStatus
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SecondaryCta
import com.peyya.app.ui.media.PeyyaImage
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaDanger
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft
import com.peyya.app.ui.theme.PeyyaSpace
import com.peyya.app.viewmodel.TripUiState

@Composable
fun PhoneMyScreen(
    state: TripUiState,
    contentPadding: PaddingValues,
    onUpdateDisplayName: (String) -> Unit,
    onUpdateVehicleModel: (String) -> Unit,
    onUpdateVehiclePlate: (String) -> Unit,
    onUpdateFamilyName: (String) -> Unit,
    onResetTrip: () -> Unit,
    onGoPlan: () -> Unit,
) {
    var nameDraft by remember(state.profile.displayName) { mutableStateOf(state.profile.displayName) }
    var modelDraft by remember(state.profile.vehicleModel) { mutableStateOf(state.profile.vehicleModel) }
    var plateDraft by remember(state.profile.vehiclePlate) { mutableStateOf(state.profile.vehiclePlate) }
    var familyDraft by remember(state.family.familyName) { mutableStateOf(state.family.familyName) }
    var showReset by remember { mutableStateOf(false) }
    val past = state.pastSessions.filter {
        it.status == TripReviewSessionStatus.Completed || it.status == TripReviewSessionStatus.Skipped
    }
    val insets = phoneContentInsets(contentPadding)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PeyyaCanvas),
        contentPadding = insets,
        verticalArrangement = Arrangement.spacedBy(PeyyaSpace.md),
    ) {
        item {
            Text("마이", color = PeyyaInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "내 정보와 내가 남긴 방문 기록을 봅니다.",
                color = PeyyaInkSub,
                fontSize = 14.sp,
            )
        }

        item {
            ProfileHero()
        }

        item {
            SectionTitle("내 정보")
            Spacer(Modifier.height(PeyyaSpace.xs))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, PeyyaLine, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FieldLabel("이름")
                ProfileField(value = nameDraft, onValueChange = { nameDraft = it })
                FieldLabel("차량")
                ProfileField(value = modelDraft, onValueChange = { modelDraft = it })
                FieldLabel("번호판")
                ProfileField(value = plateDraft, onValueChange = { plateDraft = it })
                FieldLabel("가족 이름")
                ProfileField(value = familyDraft, onValueChange = { familyDraft = it })
                Text(
                    state.profile.vehicleNote,
                    color = PeyyaInkDim,
                    fontSize = 12.sp,
                )
                PrimaryCta(
                    label = "내 정보 저장",
                    enabled = nameDraft.trim() != state.profile.displayName ||
                        modelDraft.trim() != state.profile.vehicleModel ||
                        plateDraft.trim() != state.profile.vehiclePlate ||
                        familyDraft.trim() != state.family.familyName,
                    onClick = {
                        onUpdateDisplayName(nameDraft.trim())
                        onUpdateVehicleModel(modelDraft.trim())
                        onUpdateVehiclePlate(plateDraft.trim())
                        onUpdateFamilyName(familyDraft.trim())
                    },
                )
            }
        }

        item {
            SectionTitle("내가 간 곳 · 리뷰")
            Spacer(Modifier.height(4.dp))
            Text(
                "완료 ${state.completedSessionCount}회",
                color = PeyyaInkDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        if (past.isEmpty()) {
            item {
                Text(
                    "아직 저장된 방문 리뷰가 없습니다. 실행에서 여정을 마치면 여기에 쌓여요.",
                    color = PeyyaInkSub,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        } else {
            items(past, key = { it.sessionId }) { session ->
                MyVisitCard(session)
            }
        }

        item {
            SectionTitle("설정")
            Spacer(Modifier.height(PeyyaSpace.xs))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, PeyyaLine, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingRow("연동 모드", if (state.mockMode) "Mock (시연)" else "Pleos 실연동")
                SettingRow("앱 버전", "0.1.0")
                SecondaryCta(label = "일정 처음부터 다시", onClick = { showReset = true })
                PrimaryCta(label = "계획으로 이동", enabled = true, onClick = onGoPlan)
            }
        }

        item { Spacer(Modifier.height(PeyyaSpace.xs)) }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("일정을 처음부터 다시 할까요?") },
            text = { Text("진행 중 루트만 초기화됩니다. 내 리뷰 기록은 유지됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReset = false
                        onResetTrip()
                        onGoPlan()
                    },
                ) { Text("다시 하기", color = PeyyaDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun ProfileHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        PeyyaImage(
            model = DemoContent.assetPath("car.jpg"),
            contentDescription = "내 차량",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            Text("MY DRIVE", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("차량과 나를 먼저 확인하세요", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MyVisitCard(session: TripReviewSession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PeyyaLine, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(session.tripTitle, color = PeyyaInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                when (session.status) {
                    TripReviewSessionStatus.Completed -> "완료"
                    TripReviewSessionStatus.Skipped -> "스킵"
                    TripReviewSessionStatus.InProgress -> "진행"
                },
                color = PeyyaBrand,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        session.stopReviews.forEach { review ->
            val photo = DemoContent.placePhotoByStopId[review.stopId]
                ?: DemoContent.assetPath("road.jpg")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PeyyaImage(
                    model = photo,
                    contentDescription = review.stopName,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.stopName, color = PeyyaInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(review.ratingStars) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFE6A700),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    if (review.comment.isNotBlank()) {
                        Text(review.comment, color = PeyyaInkSub, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = PeyyaInk, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = PeyyaInkDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun ProfileField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = PeyyaInk, fontSize = 16.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PeyyaSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = PeyyaInkSub, fontSize = 14.sp)
        Text(value, color = PeyyaInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
