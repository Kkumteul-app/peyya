package com.peyya.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaSoft

@Composable
fun AssetPhoto(
    model: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val data = remember(model) {
        val path = model
            .removePrefix("file:///android_asset/")
            .removePrefix("asset:///")
        runCatching { context.assets.open(path).use { it.readBytes() } }.getOrNull() ?: model
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(PeyyaSoft),
    )
}

@Composable
fun PhotoHeroBanner(
    imageModel: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Int = 168,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        AssetPhoto(imageModel, Modifier.fillMaxSize(), title)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                    ),
                ),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PhotoBleedOnboarding(
    imageModel: String,
    brand: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        AssetPhoto(imageModel, Modifier.fillMaxSize(), brand)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(0.15f),
                        0.45f to Color.Black.copy(0.35f),
                        1f to Color.Black.copy(0.78f),
                    ),
                ),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 22.dp, vertical = 28.dp),
        ) {
            Text(brand, color = Color.White, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = Color.White.copy(0.88f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun OverlapPhotoCards(
    leftTitle: String,
    leftImage: String,
    rightTitle: String,
    rightImage: String,
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(end = 8.dp),
    ) {
        PhotoPlaceCard(
            title = leftTitle,
            imageModel = leftImage,
            modifier = Modifier
                .width(168.dp)
                .height(200.dp)
                .align(Alignment.BottomStart),
            onClick = onLeft,
        )
        PhotoPlaceCard(
            title = rightTitle,
            imageModel = rightImage,
            modifier = Modifier
                .width(168.dp)
                .height(200.dp)
                .align(Alignment.TopEnd)
                .offset(y = (-8).dp)
                .shadow(10.dp, RoundedCornerShape(20.dp)),
            onClick = onRight,
        )
    }
}

@Composable
fun PhotoPlaceCard(
    title: String,
    imageModel: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(128.dp),
            ) {
                AssetPhoto(imageModel, Modifier.fillMaxSize(), title)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.55f)),
                            ),
                        ),
                )
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                )
            }
            if (meta != null) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun RoutePhotoRow(
    title: String,
    imageModel: String,
    meta: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                AssetPhoto(imageModel, Modifier.fillMaxSize(), title)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.65f)),
                            ),
                        ),
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                ) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(meta, color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (actionLabel != null && onAction != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    androidx.compose.material3.TextButton(onClick = onAction) {
                        Text(actionLabel, color = PeyyaBrand)
                    }
                }
            }
        }
    }
}
