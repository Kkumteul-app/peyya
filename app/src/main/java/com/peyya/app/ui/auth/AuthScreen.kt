package com.peyya.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.peyya.app.data.AppRepository
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.MockVehicleLookup
import com.peyya.app.domain.PlacePins
import com.peyya.app.domain.VehicleLookupPort
import com.peyya.app.domain.VehicleProfile
import com.peyya.app.ui.components.PhotoBleedOnboarding
import com.peyya.app.ui.components.PlaceSearchField
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.SoftCard
import com.peyya.app.ui.theme.PeyyaBrand

private enum class AuthMode { Welcome, Login, Signup }

@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onSignup: (email: String, password: String, FamilyProfile, VehicleProfile, PlacePins) -> Boolean,
    vehicleLookup: VehicleLookupPort = MockVehicleLookup,
) {
    var mode by remember { mutableStateOf(AuthMode.Welcome) }
    when (mode) {
        AuthMode.Welcome -> WelcomePane(
            onLogin = { mode = AuthMode.Login },
            onSignup = { mode = AuthMode.Signup },
        )
        AuthMode.Login -> LoginPane(
            onBack = { mode = AuthMode.Welcome },
            onSubmit = onLogin,
            onGoSignup = { mode = AuthMode.Signup },
        )
        AuthMode.Signup -> SignupPane(
            onBack = { mode = AuthMode.Welcome },
            onSubmit = onSignup,
            vehicleLookup = vehicleLookup,
        )
    }
}

@Composable
private fun WelcomePane(onLogin: () -> Unit, onSignup: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PhotoBleedOnboarding(
            imageModel = PeyyaImages.ONBOARD,
            brand = "Peyya",
            title = "가족과 차로 이어지는 주말",
            subtitle = "로그인하거나 가입해서 차량·집·회사를 등록하세요.",
            modifier = Modifier.weight(1f),
        )
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PrimaryCta("회원가입", true, onClick = onSignup)
            PrimaryCta("로그인", true, onClick = onLogin)
            Spacer(Modifier.height(4.dp))
            Text(
                "데모: ${AppRepository.DEMO_EMAIL} / ${AppRepository.DEMO_PASSWORD}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoginPane(
    onBack: () -> Unit,
    onSubmit: (String, String) -> Boolean,
    onGoSignup: () -> Unit,
) {
    var email by remember { mutableStateOf(AppRepository.DEMO_EMAIL) }
    var password by remember { mutableStateOf(AppRepository.DEMO_PASSWORD) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        TextButton(onClick = onBack) { Text("← 뒤로") }
        Text("로그인", style = MaterialTheme.typography.headlineMedium, color = PeyyaBrand)
        Spacer(Modifier.height(16.dp))
        SoftCard {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            PrimaryCta("로그인", email.isNotBlank() && password.length >= 4) {
                if (!onSubmit(email.trim(), password)) {
                    error = "이메일 또는 비밀번호를 확인해 주세요."
                }
            }
        }
        TextButton(onClick = onGoSignup) { Text("계정이 없나요? 회원가입") }
    }
}

@Composable
private fun SignupPane(
    onBack: () -> Unit,
    onSubmit: (String, String, FamilyProfile, VehicleProfile, PlacePins) -> Boolean,
    vehicleLookup: VehicleLookupPort,
) {
    var step by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("우리 가족") }
    var members by remember { mutableStateOf(listOf(FamilyMember("부모", AgeBand.ADULT))) }
    var newMember by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var nick by remember { mutableStateOf("") }
    var typeHint by remember { mutableStateOf("SUV") }
    var modelName by remember { mutableStateOf("") }
    var lookupNote by remember { mutableStateOf<String?>(null) }
    var home by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        TextButton(onClick = {
            if (step == 0) onBack() else step -= 1
        }) { Text("← 뒤로") }
        Text("회원가입 ${step + 1}/4", style = MaterialTheme.typography.headlineSmall, color = PeyyaBrand)
        Spacer(Modifier.height(14.dp))
        SoftCard {
            when (step) {
                0 -> {
                    Text("계정", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("이메일") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호 (4자 이상)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
                1 -> {
                    Text("가족", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("가족 이름") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    members.forEach { m ->
                        Text("${m.name} · ${m.ageBand.labelKo}", modifier = Modifier.padding(top = 6.dp))
                    }
                    OutlinedTextField(
                        value = newMember,
                        onValueChange = { newMember = it },
                        label = { Text("멤버 추가") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AgeBand.entries.forEach { band ->
                            AssistChip(
                                onClick = {
                                    val n = newMember.trim()
                                    if (n.isNotEmpty()) {
                                        members = members + FamilyMember(n, band)
                                        newMember = ""
                                    }
                                },
                                label = { Text(band.labelKo) },
                            )
                        }
                    }
                }
                2 -> {
                    Text("차량", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "번호로 조회 (Mock · 실서비스는 소유자 동의 후)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it; lookupNote = null },
                        label = { Text("차량 번호") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = {
                                val hit = vehicleLookup.lookup(plate)
                                if (hit == null) {
                                    lookupNote = "형식을 확인해 주세요 (예: 12가3456)"
                                } else {
                                    modelName = hit.modelName
                                    typeHint = hit.typeHint
                                    if (nick.isBlank()) nick = hit.modelName
                                    lookupNote = "${hit.modelName} · ${hit.typeHint} · ${hit.yearHint} (Mock)"
                                }
                            }) { Text("조회") }
                        },
                    )
                    lookupNote?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = PeyyaBrand)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nick,
                        onValueChange = { nick = it },
                        label = { Text("차량 애칭") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = typeHint,
                        onValueChange = { typeHint = it },
                        label = { Text("차종") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                else -> {
                    Text("집 · 회사", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "동·역·건물명으로 검색해 선택하세요 (시연용 목록)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    PlaceSearchField(
                        label = "집 주소 검색",
                        value = home,
                        onValueChange = { home = it },
                    )
                    Spacer(Modifier.height(12.dp))
                    PlaceSearchField(
                        label = "회사 주소 검색",
                        value = work,
                        onValueChange = { work = it },
                    )
                }
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            PrimaryCta(
                if (step < 3) "다음" else "시작하기",
                enabled = when (step) {
                    0 -> email.contains("@") && password.length >= 4
                    1 -> familyName.isNotBlank() && members.isNotEmpty()
                    2 -> nick.isNotBlank()
                    else -> true
                },
            ) {
                if (step < 3) {
                    step += 1
                    return@PrimaryCta
                }
                val ok = onSubmit(
                    email.trim(),
                    password,
                    FamilyProfile(familyName.trim(), members),
                    VehicleProfile(
                        nickname = nick.trim().ifBlank { "우리 차" },
                        typeHint = typeHint.trim().ifBlank { "SUV" },
                        plate = plate.replace(" ", ""),
                        modelName = modelName,
                    ),
                    PlacePins(homeAddress = home.trim(), workAddress = work.trim()),
                )
                if (!ok) error = "가입에 실패했습니다. 입력을 확인해 주세요."
            }
        }
    }
}
