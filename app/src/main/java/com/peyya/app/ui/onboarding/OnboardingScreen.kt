package com.peyya.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.VehicleProfile
import com.peyya.app.ui.components.AssetPhoto
import com.peyya.app.ui.components.PrimaryCta

private data class OnboardPage(
    val image: String,
    val title: String,
    val body: String,
)

@Composable
fun OnboardingScreen(
    initialFamily: FamilyProfile,
    initialVehicle: VehicleProfile,
    onFinished: (FamilyProfile, VehicleProfile) -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    var familyName by remember { mutableStateOf(initialFamily.familyName) }
    var vehicleNick by remember { mutableStateOf(initialVehicle.nickname) }
    val pages = listOf(
        OnboardPage(PeyyaImages.ONBOARD, "내비는 길을,", "페이야는 가족의 다음 장소를 이어 줍니다."),
        OnboardPage(PeyyaImages.MUSEUM, "계획에서 시동까지", "주말 동선을 짜고, 차에 타면 다음 장소를 불러 줍니다."),
        OnboardPage(PeyyaImages.FAMILY_CAR, "패밀리 맞춤", "동승자와 차량 정보를 기억해 루트를 맞춥니다."),
    )

    Box(Modifier.fillMaxSize()) {
        if (page < 3) {
            val current = pages[page]
            AssetPhoto(current.image, Modifier.fillMaxSize(), current.title)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f)),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(22.dp),
            ) {
                Text("Peyya", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(current.title, color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(10.dp))
                Text(current.body, color = Color.White.copy(0.92f), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
                        Box(
                            Modifier
                                .size(if (index == page) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == page) Color.White else Color.White.copy(0.35f)),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                PrimaryCta("다음", true) { page += 1 }
                TextButton(onClick = { onFinished(initialFamily, initialVehicle) }) {
                    Text("건너뛰기", color = Color.White)
                }
            }
        } else {
            AssetPhoto(PeyyaImages.CITY, Modifier.fillMaxSize(), "setup")
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.55f)))
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(22.dp),
            ) {
                Text("Peyya", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                Text("빠른 설정", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("가족·차량 이름은 언제든 마이에서 바꿀 수 있어요.", color = Color.White.copy(0.85f))
                Spacer(Modifier.height(20.dp))
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(0.75f),
                    cursorColor = Color.White,
                )
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("가족 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = vehicleNick,
                    onValueChange = { vehicleNick = it },
                    label = { Text("차량 별명") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                )
                Spacer(Modifier.weight(1f))
                PrimaryCta("시작하기", true) {
                    val family = FamilyProfile(
                        familyName = familyName.trim().ifEmpty { initialFamily.familyName },
                        members = initialFamily.members.ifEmpty {
                            listOf(FamilyMember("나", AgeBand.ADULT))
                        },
                    )
                    val vehicle = initialVehicle.copy(
                        nickname = vehicleNick.trim().ifEmpty { initialVehicle.nickname },
                    )
                    onFinished(family, vehicle)
                }
            }
        }
    }
}
