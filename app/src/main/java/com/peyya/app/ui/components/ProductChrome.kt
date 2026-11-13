package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.peyya.app.domain.StopCategory
import com.peyya.app.ui.theme.FoodDeep
import com.peyya.app.ui.theme.FoodMid
import com.peyya.app.ui.theme.MuseumDeep
import com.peyya.app.ui.theme.MuseumMid
import com.peyya.app.ui.theme.PeyyaBrand

@Composable
fun ScreenScaffold(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        content = content,
    )
}

@Composable
fun BrandHeader(
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Peyya", style = MaterialTheme.typography.titleLarge, color = PeyyaBrand)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}

@Composable
fun HeroBanner(
    title: String,
    subtitle: String,
    tone: StopCategory = StopCategory.Culture,
    modifier: Modifier = Modifier,
) {
    val brush = when (tone) {
        StopCategory.Food, StopCategory.Cafe -> Brush.linearGradient(listOf(FoodMid, FoodDeep))
        StopCategory.Park -> Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF047857)))
        StopCategory.Shopping -> Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF6D28D9)))
        StopCategory.Culture -> Brush.linearGradient(listOf(MuseumMid, MuseumDeep))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .padding(18.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.88f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}
