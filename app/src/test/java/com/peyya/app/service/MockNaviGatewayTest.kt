package com.peyya.app.service

import com.peyya.app.domain.Stop
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockNaviGatewayTest {

    private fun sampleStop() = Stop(
        id = "s1",
        name = "카페",
        address = "서울",
        latitude = 37.5,
        longitude = 127.0,
        poiId = "poi-1",
    )

    @Test
    fun requestRoute_then_injectArrived_emitsArrivalAndSnapshot() = runTest {
        val navi = MockNaviGateway()
        assertTrue(navi.initialize().isSuccess)
        assertTrue(navi.requestRoute(sampleStop()).isSuccess)

        val arrival = async { navi.arrivalEvents().first() }
        yield()
        navi.injectArrived()
        arrival.await()

        val snapshot = navi.pollGuidance().getOrThrow()
        assertTrue(snapshot.isArrived)
        assertEquals(0.0, snapshot.remainingMeters, 0.0)
    }

    @Test
    fun setRemainingMeters_zero_yieldsPollSnapshot() {
        val navi = MockNaviGateway()
        assertTrue(navi.initialize().isSuccess)
        assertTrue(navi.requestRoute(sampleStop()).isSuccess)

        navi.setRemainingMeters(0.0)

        val snapshot = navi.pollGuidance().getOrThrow()
        assertEquals(0.0, snapshot.remainingMeters, 0.0)
        assertFalse(snapshot.isArrived)
    }
}
