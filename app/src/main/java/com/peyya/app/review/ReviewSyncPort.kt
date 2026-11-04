package com.peyya.app.review

/**
 * 선택적 원격 동기화. 기본은 no-op.
 * 추천 경로는 이 포트를 await하지 않는다 (로컬 우선).
 */
fun interface ReviewSyncPort {
    fun sync(session: TripReviewSession)

    companion object {
        val NoOp: ReviewSyncPort = ReviewSyncPort { }
    }
}
