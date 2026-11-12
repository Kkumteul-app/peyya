package com.peyya.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaCanvas
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaLine

@Composable
fun PhoneBottomBar(
    selected: PhoneTab,
    onSelect: (PhoneTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = PeyyaLine)
        NavigationBar(
            containerColor = PeyyaCanvas,
            contentColor = PeyyaInk,
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets,
        ) {
            PhoneTab.entries.forEach { tab ->
                val selectedTab = tab == selected
                NavigationBarItem(
                    selected = selectedTab,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeyyaBrand,
                        selectedTextColor = PeyyaBrand,
                        unselectedIconColor = PeyyaInkDim,
                        unselectedTextColor = PeyyaInkDim,
                        indicatorColor = PeyyaBrand.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

private val PhoneTab.icon: ImageVector
    get() = when (this) {
        PhoneTab.Plan -> Icons.Rounded.Map
        PhoneTab.Execute -> Icons.Rounded.DirectionsCar
        PhoneTab.Recommend -> Icons.Rounded.TravelExplore
        PhoneTab.My -> Icons.Rounded.Person
    }
