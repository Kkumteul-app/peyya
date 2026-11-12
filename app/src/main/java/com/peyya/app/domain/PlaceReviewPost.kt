package com.peyya.app.domain

/**
 * 추천 탭 리뷰 피드 카드. 사진·동행·별점을 함께 보여 준다.
 */
data class PlaceReviewPost(
    val id: String,
    val placeName: String,
    val placeMeta: String,
    val photoAsset: String,
    val authorName: String,
    val companion: CompanionType,
    val stars: Int,
    val comment: String,
    val visitedLabel: String,
) {
    init {
        require(stars in 1..5)
    }
}
