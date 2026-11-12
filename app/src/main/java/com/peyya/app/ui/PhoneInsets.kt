package com.peyya.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.peyya.app.ui.theme.PeyyaSpace

/**
 * Scaffold contentPadding + 화면 거터를 합친 Phone 공통 inset.
 */
@Composable
fun phoneContentInsets(
    scaffoldPadding: PaddingValues,
    horizontalGutter: Dp = PeyyaSpace.screenGutter,
    topExtra: Dp = PeyyaSpace.sm,
    bottomExtra: Dp = PeyyaSpace.xs,
): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = scaffoldPadding.calculateStartPadding(direction) + horizontalGutter,
        top = scaffoldPadding.calculateTopPadding() + topExtra,
        end = scaffoldPadding.calculateEndPadding(direction) + horizontalGutter,
        bottom = scaffoldPadding.calculateBottomPadding() + bottomExtra,
    )
}
