package com.peyya.app.ui.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PeyyaImages
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.FamilyProfile
import com.peyya.app.domain.MockVehicleLookup
import com.peyya.app.domain.PlacePins
import com.peyya.app.domain.VehicleProfile
import com.peyya.app.ui.components.FamilyAvatars
import com.peyya.app.ui.components.PhotoHeroBanner
import com.peyya.app.ui.components.PlaceSearchField
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.components.ScreenScaffold
import com.peyya.app.ui.components.SectionTitle
import com.peyya.app.ui.components.SoftCard
import com.peyya.app.ui.theme.PeyyaBrand

@Composable
fun MyScreen(
    contentPadding: PaddingValues,
    family: FamilyProfile,
    vehicle: VehicleProfile,
    places: PlacePins,
    onSaveFamily: (FamilyProfile) -> Unit,
    onSaveVehicle: (VehicleProfile) -> Unit,
    onSavePlaces: (PlacePins) -> Unit,
    onLogout: () -> Unit,
) {
    var familyName by remember(family.familyName) { mutableStateOf(family.familyName) }
    var members by remember(family.members) { mutableStateOf(family.members) }
    var nick by remember(vehicle.nickname) { mutableStateOf(vehicle.nickname) }
    var typeHint by remember(vehicle.typeHint) { mutableStateOf(vehicle.typeHint) }
    var plate by remember(vehicle.plate) { mutableStateOf(vehicle.plate) }
    var modelName by remember(vehicle.modelName) { mutableStateOf(vehicle.modelName) }
    var home by remember(places.homeAddress) { mutableStateOf(places.homeAddress) }
    var work by remember(places.workAddress) { mutableStateOf(places.workAddress) }
    var newMember by remember { mutableStateOf("") }
    var lookupNote by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(contentPadding) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            PhotoHeroBanner(
                imageModel = PeyyaImages.FAMILY_CAR,
                title = family.familyName,
                subtitle = "${vehicle.nickname} · ${vehicle.typeHint}",
                height = 140,
            )
            Spacer(Modifier.height(16.dp))
            SectionTitle("가족")
            Spacer(Modifier.height(8.dp))
            SoftCard {
                FamilyAvatars(members)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("가족 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                members.forEach { member ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${member.name} · ${member.ageBand.labelKo}")
                        TextButton(onClick = {
                            if (members.size > 1) members = members.filterNot { it == member }
                        }) { Text("삭제") }
                    }
                }
                OutlinedTextField(
                    value = newMember,
                    onValueChange = { newMember = it },
                    label = { Text("멤버 이름 추가") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AgeBand.entries.forEach { band ->
                        AssistChip(
                            onClick = {
                                val name = newMember.trim()
                                if (name.isNotEmpty()) {
                                    members = members + FamilyMember(name, band)
                                    newMember = ""
                                }
                            },
                            label = { Text(band.labelKo) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                PrimaryCta("가족 저장", true) {
                    onSaveFamily(
                        FamilyProfile(
                            familyName = familyName.trim().ifEmpty { family.familyName },
                            members = members,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            SectionTitle("차량")
            Spacer(Modifier.height(8.dp))
            SoftCard {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it; lookupNote = null },
                    label = { Text("차량 번호") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = {
                            val hit = MockVehicleLookup.lookup(plate)
                            if (hit == null) {
                                lookupNote = "형식을 확인해 주세요"
                            } else {
                                modelName = hit.modelName
                                typeHint = hit.typeHint
                                if (nick.isBlank()) nick = hit.modelName
                                lookupNote = "${hit.modelName} (Mock)"
                            }
                        }) { Text("조회") }
                    },
                )
                lookupNote?.let {
                    Text(it, color = PeyyaBrand, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = { Text("별명") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = typeHint,
                    onValueChange = { typeHint = it },
                    label = { Text("차종 힌트") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                PrimaryCta("차량 저장", true) {
                    onSaveVehicle(
                        vehicle.copy(
                            nickname = nick.trim().ifEmpty { vehicle.nickname },
                            typeHint = typeHint.trim().ifEmpty { vehicle.typeHint },
                            plate = plate.replace(" ", ""),
                            modelName = modelName,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            SectionTitle("집 · 회사")
            Spacer(Modifier.height(8.dp))
            SoftCard {
                PlaceSearchField(label = "집 주소 검색", value = home, onValueChange = { home = it })
                Spacer(Modifier.height(12.dp))
                PlaceSearchField(label = "회사 주소 검색", value = work, onValueChange = { work = it })
                Spacer(Modifier.height(10.dp))
                PrimaryCta("장소 저장", true) {
                    onSavePlaces(PlacePins(homeAddress = home.trim(), workAddress = work.trim()))
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onLogout) {
                Text("로그아웃", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
