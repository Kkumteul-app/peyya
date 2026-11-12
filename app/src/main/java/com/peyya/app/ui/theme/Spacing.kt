package com.peyya.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Phone spacing scale. CTA·섹션·ActionDock은 이 토큰만 사용한다.
 */
object PeyyaSpace {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp

    /** Primary ↔ Secondary CTA */
    val ctaGap = xs

    /** ActionDock 상단 (콘텐츠와 결정 영역 분리) */
    val dockTop = sm

    /** ActionDock 하단 (BottomNav 직전) */
    val dockBottom = xs

    /** 화면 좌우 기본 거터 */
    val screenGutter = lg
}
