package com.peyya.app.service

import com.peyya.app.domain.Stop
import kotlinx.coroutines.flow.Flow

interface NaviGateway {
    val isMock: Boolean

    fun initialize(): Result<Unit>

    fun requestRoute(stop: Stop): Result<Unit>

    /** 안내 스냅샷 폴링. 미지원·미수신이면 failure */
    fun pollGuidance(): Result<NaviGuidanceSnapshot>

    /** 목적지 도착 이벤트. 미지원이면 empty + 로그 */
    fun arrivalEvents(): Flow<Unit>

    fun release()
}
