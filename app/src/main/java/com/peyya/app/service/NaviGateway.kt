package com.peyya.app.service

import com.peyya.app.domain.Stop

interface NaviGateway {
    val isMock: Boolean

    fun initialize(): Result<Unit>

    fun requestRoute(stop: Stop): Result<Unit>

    fun release()
}
