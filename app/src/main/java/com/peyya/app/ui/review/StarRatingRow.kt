package com.peyya.app.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaLine

@Composable
fun StarRatingRow(
    stars: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: TextUnit = 40.sp,
    touchSize: Dp = 48.dp,
    activeColor: Color = PeyyaBrand,
    inactiveColor: Color = PeyyaLine,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = if (stars in 1..5) "별점 ${stars}점" else "별점 미선택"
        },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (value in 1..5) {
            val on = stars >= value
            Text(
                text = "★",
                color = if (on) activeColor else inactiveColor,
                fontSize = starSize,
                modifier = Modifier
                    .size(touchSize)
                    .clickable { onSelect(value) },
            )
        }
    }
}
