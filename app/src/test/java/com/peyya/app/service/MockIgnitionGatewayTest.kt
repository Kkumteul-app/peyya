package com.peyya.app.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockIgnitionGatewayTest {

    @Test
    fun ignitionState_startsOff_thenSetOn_emitsOn() = runTest {
        val ignition = MockIgnitionGateway()
        assertTrue(ignition.initialize().isSuccess)

        assertEquals(IgnitionState.Off, ignition.ignitionState().first())

        ignition.setOn()

        assertEquals(IgnitionState.On, ignition.ignitionState().first())
    }

    @Test
    fun setOn_thenSetOff_returnsToOff() = runTest {
        val ignition = MockIgnitionGateway()
        assertTrue(ignition.initialize().isSuccess)

        ignition.setOn()
        assertEquals(IgnitionState.On, ignition.ignitionState().first())

        ignition.setOff()
        assertEquals(IgnitionState.Off, ignition.ignitionState().first())
    }
}
