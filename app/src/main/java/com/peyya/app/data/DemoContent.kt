package com.peyya.app.data

import com.peyya.app.domain.CompanionType
import com.peyya.app.domain.PlaceReviewPost
import com.peyya.app.domain.UserProfile

object DemoContent {
    val defaultProfile = UserProfile(
        displayName = "김민수",
        vehicleModel = "Hyundai IONIQ 5",
        vehiclePlate = "12가 3456",
        vehicleNote = "주말 패밀리 시트 · 유모차 수납",
    )

    // assets/places 이미지. Coil: file:///android_asset/places/<file>
    fun assetPath(fileName: String): String = "file:///android_asset/places/$fileName"

    val placePhotoByStopId: Map<String, String> = mapOf(
        "museum" to assetPath("museum.jpg"),
        "restaurant" to assetPath("food.jpg"),
        "restaurant-first" to assetPath("food.jpg"),
        "museum-second" to assetPath("museum.jpg"),
        "cafe-hannam" to assetPath("cafe.jpg"),
        "food-yongsan-bbq" to assetPath("food.jpg"),
        "cafe-ichon" to assetPath("cafe.jpg"),
        "culture-war-memorial" to assetPath("museum.jpg"),
        "yongsan-station" to assetPath("road.jpg"),
        "namsan" to assetPath("park.jpg"),
        "hangang" to assetPath("park.jpg"),
        "home" to assetPath("road.jpg"),
        "namsan-near" to assetPath("park.jpg"),
        "hangang-near" to assetPath("park.jpg"),
        "recommend-family" to assetPath("park.jpg"),
        "recommend-market" to assetPath("road.jpg"),
    )

    val communityReviews: List<PlaceReviewPost> = listOf(
        PlaceReviewPost(
            id = "r1",
            placeName = "국립중앙박물관",
            placeMeta = "용산 · 문화",
            photoAsset = assetPath("museum.jpg"),
            authorName = "이서연",
            companion = CompanionType.FAMILY,
            stars = 5,
            comment = "아이랑 특별전이 딱이었어요. 다음엔 점심까지 이어서 잡으려요.",
            visitedLabel = "지난 주말",
        ),
        PlaceReviewPost(
            id = "r2",
            placeName = "용산 한식 담소",
            placeMeta = "식사 · 박물관 인근",
            photoAsset = assetPath("food.jpg"),
            authorName = "박준호",
            companion = CompanionType.COUPLE,
            stars = 4,
            comment = "대기 짧고 반찬이 정갈해요. 연인이랑 오기 좋은 온도.",
            visitedLabel = "2주 전",
        ),
        PlaceReviewPost(
            id = "r3",
            placeName = "남산 산책 코스",
            placeMeta = "야외 · 가볍게",
            photoAsset = assetPath("park.jpg"),
            authorName = "최유진",
            companion = CompanionType.ALONE,
            stars = 5,
            comment = "혼자 머리 식히기 좋아요. 해가 질 때 뷰가 특히.",
            visitedLabel = "이번 달",
        ),
        PlaceReviewPost(
            id = "r4",
            placeName = "한남 카페 골목",
            placeMeta = "카페 · 짧은 쉼",
            photoAsset = assetPath("cafe.jpg"),
            authorName = "정하린",
            companion = CompanionType.FAMILY,
            stars = 4,
            comment = "시니어 부모님도 계단 없이 들어가기 편했어요.",
            visitedLabel = "지난달",
        ),
    )
}
