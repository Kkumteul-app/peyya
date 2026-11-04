package com.peyya.app.progress

/**
 * Arrived 이후 다음 장소 제안 타이밍. 순수 세션 상태만 유지하며 I/O 없음.
 *
 * 같은 Arrived 세션에서 제안은 1회:
 * - 시동 OFF→ON
 * - 시동 유지 [dwellMs] (기본 5분)
 */
class NextLegPrompt(
    private val dwellMs: Long = 5 * 60 * 1000L,
) {
    private var arrivedAtMs: Long? = null
    private var proposed: Boolean = false
    private var sawOff: Boolean = false

    /** Arrived 세션 시작. 제안 플래그를 리셋한다. */
    fun onArrived(ignitionOn: Boolean, atMs: Long) {
        arrivedAtMs = atMs
        proposed = false
        sawOff = !ignitionOn
    }

    fun onIgnitionOff() {
        if (arrivedAtMs == null) return
        sawOff = true
    }

    /**
     * @return true면 지금 다음 장소 제안 (OFF 이후 ON, 아직 미제안)
     */
    fun onIgnitionOn(now: Long): Boolean {
        if (arrivedAtMs == null || proposed || !sawOff) return false
        proposed = true
        return true
    }

    /**
     * @return true면 지금 다음 장소 제안 (시동 유지 [dwellMs], OFF를 본 적 없음)
     */
    fun onTick(now: Long, ignitionStillOn: Boolean): Boolean {
        val started = arrivedAtMs ?: return false
        if (proposed || sawOff || !ignitionStillOn) return false
        if (now - started < dwellMs) return false
        proposed = true
        return true
    }
}
