package com.peyya.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.ui.graphics.vector.ImageVector

enum class PeyyaTab(
    val label: String,
    val icon: ImageVector,
) {
    Execute("실행", Icons.Rounded.Navigation),
    Plan("계획", Icons.Rounded.Explore),
    Recommend("추천", Icons.Rounded.AutoAwesome),
    My("마이", Icons.Rounded.AccountCircle),
}
