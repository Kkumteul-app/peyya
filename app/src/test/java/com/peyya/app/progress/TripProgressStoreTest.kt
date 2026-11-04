package com.peyya.app.progress

import com.peyya.app.domain.TripPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TripProgressStoreTest {

    private val today: LocalDate = LocalDate.of(2026, 11, 4)

    @Test
    fun saveAndLoad_snoozeUntil() {
        val store: TripProgressStore = InMemoryTripProgressStore()
        val until = 1_700_000_000_000L
        store.save(
            TripProgressSnapshot(
                tripId = "trip-demo",
                phase = TripPhase.Idle,
                snoozeUntilEpochMs = until,
            ),
        )

        val loaded = store.load()
        assertEquals(until, loaded?.snoozeUntilEpochMs)
        assertEquals("trip-demo", loaded?.tripId)
        assertEquals(TripPhase.Idle, loaded?.phase)
    }

    @Test
    fun skipToday_staleDate_clearsOnNormalize() {
        val yesterday = today.minusDays(1)
        val stored = TripProgressSnapshot(
            tripId = "trip-demo",
            phase = TripPhase.Idle,
            skipTodayDate = yesterday,
        )

        val normalized = stored.clearedSkipIfStale(today)
        assertNull(normalized.skipTodayDate)

        val stillToday = stored.copy(skipTodayDate = today).clearedSkipIfStale(today)
        assertEquals(today, stillToday.skipTodayDate)
    }

    @Test
    fun processRestart_simulation_restoresAllFields() {
        val firstProcess: TripProgressStore = InMemoryTripProgressStore()
        val snapshot = TripProgressSnapshot(
            tripId = "family-day",
            phase = TripPhase.Arrived(stopId = "stop-1"),
            stopId = "stop-1",
            snoozeUntilEpochMs = 42L,
            skipTodayDate = today,
            reservationAtEpochMs = 99L,
            nextLegProposed = true,
            lastArrivalPath = ArrivalPath.Poll,
        )
        firstProcess.save(snapshot)

        // 프로세스 킬 후 영속 내용을 새 스토어 인스턴스에 복원하는 상황을 흉내낸다.
        val afterRestart = InMemoryTripProgressStore().also { it.save(firstProcess.load()!!) }
        val loaded = afterRestart.load()!!

        assertEquals(snapshot, loaded)
        assertTrue(loaded.nextLegProposed)
        assertEquals(ArrivalPath.Poll, loaded.lastArrivalPath)
        assertEquals(99L, loaded.reservationAtEpochMs)
        // GPS 좌표 필드가 DTO에 없음을 toString 계약으로 확인 (lat/lng 미포함).
        assertTrue(!loaded.toString().contains("lat", ignoreCase = true))
        assertTrue(!loaded.toString().contains("lng", ignoreCase = true))
    }
}
