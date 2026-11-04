package com.peyya.app.progress

/**
 * 도착 확정에 사용한 경로.
 * 출발 후 경과 시간·최초 ETA만으로 확정하는 경로는 존재하지 않는다.
 */
enum class ArrivalPath {
    /** 내비 도착 이벤트 */
    Event,

    /** 안내 스냅샷 폴링 (isArrived 또는 remainingMeters <= 0) */
    Poll,

    /** GPS 체류 프롬프트 후 사용자 확인 */
    UserGpsPrompt,

    /** 메뉴 「도착했어요」 수동 확정 */
    Menu,
}
