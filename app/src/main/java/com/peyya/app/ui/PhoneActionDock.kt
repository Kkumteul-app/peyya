package com.peyya.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peyya.app.ui.theme.PeyyaSpace

/**
 * 스크롤 content와 분리된 하단 결정 영역.
 * Primary / Secondary CTA만 넣는다. BottomNav 여백은 Scaffold가 담당.
 */
@Composable
fun PhoneActionDock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = PeyyaSpace.dockTop, bottom = PeyyaSpace.dockBottom),
        verticalArrangement = Arrangement.spacedBy(PeyyaSpace.ctaGap),
        content = content,
    )
}
