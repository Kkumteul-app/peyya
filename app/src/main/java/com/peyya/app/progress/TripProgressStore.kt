package com.peyya.app.progress

/**
 * 여정 진행 스냅샷 저장소.
 *
 * Android Context 없이 단위 테스트 가능하도록 인터페이스로 분리한다.
 * 프로덕션 SharedPreferences/DataStore 구현은 Context를 주입받아 이 계약을 따른다.
 */
interface TripProgressStore {
    fun load(): TripProgressSnapshot?

    fun save(snapshot: TripProgressSnapshot)

    fun clear()
}

/** 프로세스 재시작 시뮬레이션·단위 테스트용 인메모리 구현. */
class InMemoryTripProgressStore : TripProgressStore {
    private var snapshot: TripProgressSnapshot? = null

    override fun load(): TripProgressSnapshot? = snapshot

    override fun save(snapshot: TripProgressSnapshot) {
        this.snapshot = snapshot
    }

    override fun clear() {
        snapshot = null
    }
}
