package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.domain.AgeBand
import com.peyya.app.domain.FamilyMember
import com.peyya.app.domain.Stop
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.peyya.app.ui.theme.CarBrandLight
import com.peyya.app.ui.theme.CarDim
import com.peyya.app.ui.theme.CarLineStrong
import com.peyya.app.ui.theme.CarText
import com.peyya.app.ui.theme.CarTextSub
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaDisabledBg
import com.peyya.app.ui.theme.PeyyaDisabledFg
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSoft

enum class StopState { Done, Current, Upcoming }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FamilyChips(members: List<FamilyMember>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        members.forEach { member ->
            val label = when (member.ageBand) {
                AgeBand.CHILD, AgeBand.SENIOR -> "${member.name} · ${member.ageBand.labelKo}"
                else -> member.name
            }
            Text(
                text = label,
                color = PeyyaInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(PeyyaSoft)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
fun StopSelectRow(
    stop: Stop,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit,
) {
    val dim = !stop.selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = stop.selected,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle(stop.id, it) },
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = stop.selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = PeyyaBrand),
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                stop.name,
                color = if (dim) PeyyaDisabledFg else PeyyaInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stop.address,
                color = if (dim) PeyyaDisabledFg else PeyyaInkDim,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun StopTimelineRow(
    order: Int,
    name: String,
    caption: String,
    state: StopState,
    isLast: Boolean,
    etaLabel: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhoneStopNode(order = order, state = state)
            if (!isLast) {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .heightIn(min = 24.dp)
                        .background(PeyyaLine),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
        ) {
            Text(
                name,
                color = if (state == StopState.Done) PeyyaInkDim else PeyyaInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(caption, color = PeyyaInkDim, fontSize = 13.sp)
        }
        if (!etaLabel.isNullOrBlank() && state != StopState.Done) {
            Text(
                etaLabel,
                color = if (state == StopState.Current) PeyyaBrand else PeyyaInkDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp, start = 8.dp),
            )
        }
    }
}

@Composable
private fun PhoneStopNode(order: Int, state: StopState) {
    when (state) {
        StopState.Done -> Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PeyyaInk),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        StopState.Current -> Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.5.dp, PeyyaBrand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$order",
                color = PeyyaBrand,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        StopState.Upcoming -> Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, PeyyaLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$order",
                color = PeyyaInkDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun CarStopSelectRow(
    stop: Stop,
    onToggle: (String, Boolean) -> Unit,
) {
    val dim = !stop.selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .toggleable(
                value = stop.selected,
                role = Role.Checkbox,
                onValueChange = { onToggle(stop.id, it) },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            Checkbox(
                checked = stop.selected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = PeyyaBrand,
                    uncheckedColor = CarTextSub,
                    checkmarkColor = Color.White,
                ),
            )
        }
        Text(
            stop.name,
            color = if (dim) CarDim else CarText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun CarStopTimelineRow(
    order: Int,
    name: String,
    caption: String,
    state: StopState,
    isLast: Boolean,
    etaLabel: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CarStopNode(order = order, state = state)
            if (!isLast) {
                Box(
                    Modifier
                        .padding(vertical = 6.dp)
                        .width(3.dp)
                        .height(56.dp)
                        .background(CarLineStrong),
                )
            }
        }
        Spacer(Modifier.width(18.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 20.dp),
        ) {
            Text(
                name,
                color = if (state == StopState.Done) CarTextSub else CarText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                caption,
                color = when (state) {
                    StopState.Current -> CarBrandLight
                    StopState.Done -> CarDim
                    StopState.Upcoming -> CarDim
                },
                fontSize = 18.sp,
                fontWeight = if (state == StopState.Current) FontWeight.Bold else FontWeight.Normal,
            )
        }
        if (!etaLabel.isNullOrBlank() && state != StopState.Done) {
            Text(
                etaLabel,
                color = if (state == StopState.Current) CarBrandLight else CarDim,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp, start = 12.dp),
            )
        }
    }
}

@Composable
private fun CarStopNode(order: Int, state: StopState) {
    when (state) {
        StopState.Done -> Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CarLineStrong),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = CarText, modifier = Modifier.size(22.dp))
        }
        StopState.Current -> Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PeyyaBrand),
            contentAlignment = Alignment.Center,
        ) {
            Text("$order", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        StopState.Upcoming -> Box(
            modifier = Modifier
                .size(40.dp)
                .border(2.dp, CarLineStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$order", color = CarTextSub, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrimaryCta(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    demo: Boolean = false,
    height: Dp = 56.dp,
    corner: Dp = 14.dp,
    textSize: TextUnit = 16.sp,
    textWeight: FontWeight = FontWeight.Bold,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(corner),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PeyyaBrand,
            contentColor = Color.White,
            disabledContainerColor = PeyyaDisabledBg,
            disabledContentColor = PeyyaDisabledFg,
        ),
    ) {
        Text(label, fontWeight = textWeight, fontSize = textSize)
        if (demo) {
            Spacer(Modifier.width(8.dp))
            Text(
                "데모",
                color = Color.White,
                fontSize = if (height >= 84.dp) 18.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun CarOutlinedCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .border(1.dp, CarLineStrong, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = CarText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(
            label,
            color = PeyyaInkSub,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun DemoBadge(visible: Boolean, onDark: Boolean = false) {
    if (!visible) return
    val fg = if (onDark) CarTextSub else PeyyaInkDim
    val line = if (onDark) CarLineStrong else PeyyaLine
    Text(
        "데모",
        color = fg,
        fontSize = if (onDark) 18.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .border(1.dp, line, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
