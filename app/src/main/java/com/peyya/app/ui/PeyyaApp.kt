package com.peyya.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.peyya.app.domain.TripPhase
import com.peyya.app.ui.auth.AuthScreen
import com.peyya.app.ui.components.PeyyaTopBar
import com.peyya.app.ui.execute.ExecuteScreen
import com.peyya.app.ui.my.MyScreen
import com.peyya.app.ui.navigation.PeyyaTab
import com.peyya.app.ui.plan.PlanScreen
import com.peyya.app.ui.recommend.RecommendScreen
import com.peyya.app.ui.theme.PeyyaTheme
import com.peyya.app.viewmodel.AppViewModel
import com.peyya.app.viewmodel.AppViewModelFactory
import com.peyya.app.viewmodel.TripViewModel
import com.peyya.app.viewmodel.TripViewModelFactory

@Composable
fun PeyyaApp(
    forceMock: Boolean = false,
) {
    val context = LocalContext.current
    val appVm: AppViewModel = viewModel(factory = AppViewModelFactory(context.applicationContext))
    val tripVm: TripViewModel = viewModel(
        factory = TripViewModelFactory(
            appContext = context.applicationContext,
            forceMock = forceMock,
        ),
    )
    val snapshot by appVm.snapshot.collectAsStateWithLifecycle()
    val tripState by tripVm.uiState.collectAsStateWithLifecycle()
    val carMode = isCarLayout()
    var tab by remember { mutableStateOf(PeyyaTab.Execute) }
    var driving by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var notifyMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snapshot.family) {
        tripVm.syncFamily(snapshot.family)
    }
    LaunchedEffect(tripState.phase, driving) {
        if (driving && tripState.phase is TripPhase.Completed) {
            appVm.completeActiveRoute()
        }
    }

    PeyyaTheme(carMode = carMode) {
        if (!snapshot.onboardingDone || !snapshot.auth.loggedIn) {
            AuthScreen(
                onLogin = { email, pass -> appVm.login(email, pass) },
                onSignup = { email, pass, family, vehicle, places ->
                    appVm.signup(email, pass, family, vehicle, places)
                },
            )
            return@PeyyaTheme
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!driving) {
                    PeyyaTopBar(
                        onNotifications = {
                            notifyMsg = "새 알림이 없습니다. 예정 루트가 생기면 알려 드릴게요."
                        },
                        onSettings = { showSettings = true },
                    )
                }
            },
            bottomBar = {
                if (!driving) {
                    NavigationBar {
                        PeyyaTab.entries.forEach { item ->
                            NavigationBarItem(
                                selected = tab == item,
                                onClick = { tab = item },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize()) {
                when (tab) {
                    PeyyaTab.Plan -> PlanScreen(
                        contentPadding = padding,
                        session = snapshot.plan,
                        onChip = appVm::answerPlan,
                        onQuickDestination = appVm::quickPlan,
                        onAnswer = appVm::answerPlan,
                        onSelectCandidate = appVm::selectCandidate,
                        onConfirm = {
                            appVm.confirmPlan(it)
                            tab = PeyyaTab.Execute
                        },
                        onReset = appVm::resetPlan,
                        onBack = appVm::planBack,
                    )
                    PeyyaTab.Execute -> ExecuteScreen(
                        contentPadding = padding,
                        routes = snapshot.routes,
                        vehicleNickname = snapshot.vehicle.nickname,
                        tripState = tripState,
                        driving = driving,
                        onPlanCta = { tab = PeyyaTab.Plan },
                        onStartRoute = { route ->
                            val started = appVm.startRoute(route.id) ?: route
                            tripVm.loadTrip(started.toTripPlan())
                            driving = true
                        },
                        onExitDriving = {
                            driving = false
                            if (tripState.phase is TripPhase.Completed) {
                                appVm.completeActiveRoute()
                            }
                        },
                        onToggleStop = tripVm::toggleStop,
                        onStartDriving = tripVm::startRoute,
                        onMarkArrived = tripVm::markArrived,
                        onIgnition = tripVm::onIgnition,
                        onConfirmNext = tripVm::confirmNext,
                        onSkipNext = tripVm::skipNext,
                        onResetTrip = tripVm::resetTrip,
                        onFamilyNameChange = tripVm::updateFamilyName,
                        onTogglePrep = tripVm::togglePrepItem,
                        onDismissAdapt = tripVm::dismissAdapt,
                        onAcceptParking = tripVm::acceptParkingSuggestion,
                        onVoiceChip = tripVm::applyVoiceChip,
                        onContinueReviewIntro = tripVm::continueReviewFromIntro,
                        onUpdateReview = tripVm::updateCurrentReview,
                        onNextReview = tripVm::nextReviewStop,
                        onSkipReview = tripVm::skipReview,
                        onApplyCourse = tripVm::applyCourse,
                    )
                    PeyyaTab.My -> MyScreen(
                        contentPadding = padding,
                        family = snapshot.family,
                        vehicle = snapshot.vehicle,
                        places = snapshot.places,
                        onSaveFamily = appVm::updateFamily,
                        onSaveVehicle = appVm::updateVehicle,
                        onSavePlaces = appVm::updatePlaces,
                        onLogout = {
                            appVm.logout()
                            tab = PeyyaTab.Execute
                        },
                    )
                    PeyyaTab.Recommend -> RecommendScreen(
                        contentPadding = padding,
                        routes = snapshot.community,
                        onLike = appVm::toggleLike,
                        onComment = appVm::addComment,
                        onAdopt = { id, at ->
                            appVm.adoptCommunity(id, at)
                            tab = PeyyaTab.Execute
                        },
                    )
                }
            }
        }

        notifyMsg?.let { msg ->
            AlertDialog(
                onDismissRequest = { notifyMsg = null },
                confirmButton = {
                    TextButton(onClick = { notifyMsg = null }) { Text("확인") }
                },
                title = { Text("알림") },
                text = { Text(msg) },
            )
        }
        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                confirmButton = {
                    TextButton(onClick = {
                        showSettings = false
                        tab = PeyyaTab.My
                    }) { Text("마이로 이동") }
                },
                dismissButton = {
                    TextButton(onClick = { showSettings = false }) { Text("닫기") }
                },
                title = { Text("설정") },
                text = {
                    Text(
                        "${snapshot.vehicle.nickname} · ${snapshot.auth.email.ifBlank { "계정" }}\n" +
                            "집: ${snapshot.places.homeAddress.ifBlank { "미등록" }}\n" +
                            "회사: ${snapshot.places.workAddress.ifBlank { "미등록" }}",
                    )
                },
            )
        }
    }
}
