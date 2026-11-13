package com.peyya.app.ui.execute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.ExecuteCopyPool
import com.peyya.app.domain.RouteMetricsEstimator
import com.peyya.app.domain.RouteStatus
import com.peyya.app.domain.SavedRoute
import com.peyya.app.ui.TripScreen
import com.peyya.app.ui.components.CourseRouteBoard
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.RoutePhotoRow
import com.peyya.app.ui.components.ScreenScaffold
import com.peyya.app.ui.components.SectionTitle
import com.peyya.app.ui.components.SoftCard
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.viewmodel.TripUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExecuteScreen(
    contentPadding: PaddingValues,
    routes: List<SavedRoute>,
    vehicleNickname: String,
    tripState: TripUiState,
    driving: Boolean,
    onPlanCta: () -> Unit,
    onStartRoute: (SavedRoute) -> Unit,
    onExitDriving: () -> Unit,
    onToggleStop: (String, Boolean) -> Unit,
    onStartDriving: () -> Unit,
    onMarkArrived: () -> Unit,
    onIgnition: () -> Unit,
    onConfirmNext: () -> Unit,
    onSkipNext: () -> Unit,
    onResetTrip: () -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onTogglePrep: (String) -> Unit = {},
    onDismissAdapt: () -> Unit = {},
    onAcceptParking: (String) -> Unit = {},
    onVoiceChip: (String) -> Unit = {},
    onContinueReviewIntro: () -> Unit = {},
    onUpdateReview: (Int, String) -> Unit = { _, _ -> },
    onNextReview: () -> Unit = {},
    onSkipReview: () -> Unit = {},
    onApplyCourse: (String) -> Unit = {},
) {
    if (driving) {
        Column(
            Modifier
                .padding(contentPadding)
                .padding(top = 4.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("실행 중", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onExitDriving) { Text("목록으로") }
            }
            TripScreen(
                state = tripState,
                onFamilyNameChange = onFamilyNameChange,
                onToggleStop = onToggleStop,
                onStartRoute = onStartDriving,
                onMarkArrived = onMarkArrived,
                onIgnition = onIgnition,
                onConfirmNext = onConfirmNext,
                onSkipNext = onSkipNext,
                onResetTrip = onResetTrip,
                onTogglePrep = onTogglePrep,
                onDismissAdapt = onDismissAdapt,
                onAcceptParking = onAcceptParking,
                onVoiceChip = onVoiceChip,
                onContinueReviewIntro = onContinueReviewIntro,
                onUpdateReview = onUpdateReview,
                onNextReview = onNextReview,
                onSkipReview = onSkipReview,
                onApplyCourse = onApplyCourse,
            )
        }
        return
    }

    val upcoming = routes.filter {
        it.status == RouteStatus.Scheduled || it.status == RouteStatus.Draft || it.status == RouteStatus.Active
    }
    val history = routes.filter { it.status == RouteStatus.Done }
    val fmt = SimpleDateFormat("M/d HH:mm", Locale.KOREA)
    val clock = SimpleDateFormat("HH:mm", Locale.KOREA)
    val emptyCopy = remember(vehicleNickname) { ExecuteCopyPool.pick(vehicleNickname) }
    val focus = upcoming.firstOrNull()
    val leaveWindow = remember(focus?.id) {
        focus?.let { RouteMetricsEstimator.leaveHomeWindow(it.stops) }
    }

    ScreenScaffold(contentPadding) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (focus != null && leaveWindow != null) {
                SoftCard {
                    Text("지금 나가면", style = MaterialTheme.typography.labelLarge, color = PeyyaBrand)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${clock.format(Date(leaveWindow.leaveAtEpochMs))}에 출발하면",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "${clock.format(Date(leaveWindow.arriveHomeEpochMs))}에 다시 집에 도착해요",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "이동 약 ${leaveWindow.driveMinutes}분 · 체류 약 ${leaveWindow.visitMinutes}분 (예상)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            SectionTitle("오늘의 계획")
            Spacer(Modifier.height(8.dp))
            if (upcoming.isEmpty()) {
                SoftCard {
                    Text(emptyCopy, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    PrimaryCta("계획 세우기", true, onClick = onPlanCta)
                }
            } else {
                upcoming.forEach { route ->
                    val window = RouteMetricsEstimator.leaveHomeWindow(route.stops)
                    val rangeText = window?.let {
                        "${clock.format(Date(it.leaveAtEpochMs))} 출발 → ${clock.format(Date(it.arriveHomeEpochMs))} 귀가"
                    } ?: (route.scheduledAtEpochMs?.let { fmt.format(Date(it)) } ?: "시간 미정")
                    CourseRouteBoard(
                        title = route.title,
                        subtitle = rangeText,
                        stops = route.stops,
                        heightDp = 240,
                    )
                    Spacer(Modifier.height(8.dp))
                    PrimaryCta(
                        if (route.status == RouteStatus.Draft) "일정 잡아 시작" else "지금 출발",
                        true,
                    ) { onStartRoute(route) }
                    Spacer(Modifier.height(16.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            SectionTitle("지난 루트")
            Spacer(Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text("완료한 루트가 여기 쌓입니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                history.forEach { route ->
                    RoutePhotoRow(
                        title = route.title,
                        imageModel = PeyyaImages.forStopId(route.stops.firstOrNull()?.id.orEmpty()),
                        meta = route.completedAtEpochMs?.let { "완료 ${fmt.format(Date(it))}" } ?: "완료",
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (upcoming.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onPlanCta) {
                    Text("새 계획 세우기", color = PeyyaBrand)
                }
            }
        }
    }
}
