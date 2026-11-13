package com.peyya.app.data

import com.peyya.app.domain.CommunityComment
import com.peyya.app.domain.CommunityRoute
import com.peyya.app.domain.RouteSource
import com.peyya.app.domain.RouteStatus
import com.peyya.app.domain.SavedRoute
import com.peyya.app.domain.Stop
import com.peyya.app.domain.StopCategory
import com.peyya.app.domain.VehicleProfile

data class SeedPoi(
    val stop: Stop,
    val region: String,
    val category: StopCategory,
    val tags: List<String>,
)

object SeedCatalog {
    val vehicleDefault = VehicleProfile(nickname = "패밀리 SUV", typeHint = "SUV")

    val pois: List<SeedPoi> = listOf(
        SeedPoi(
            Stop(
                id = "museum",
                name = "국립중앙박물관",
                address = "서울 용산구 서빙고로 137",
                latitude = 37.52386,
                longitude = 126.98047,
                poiId = "peyya-demo-museum",
                needsBooking = true,
                bookingUrl = "https://www.museum.go.kr/program/show/showDetail.do",
                prepChecklist = listOf("관람일·시간대 확인", "공식 예매 페이지에서 예약", "주차·동선 미리 보기"),
            ),
            "용산", StopCategory.Culture, listOf("박물관", "용산", "문화", "주말"),
        ),
        SeedPoi(
            Stop("restaurant", "용산 한식 담소 (데모)", "서울 용산구 한강대로 405 인근 (데모 POI)", 37.53120, 126.96580, "peyya-demo-restaurant"),
            "용산", StopCategory.Food, listOf("맛집", "한식", "용산", "점심"),
        ),
        SeedPoi(
            Stop("hangang-cafe", "한강 뷰 카페 라운지 (데모)", "서울 용산구 이촌로 인근", 37.52110, 126.97020, "peyya-demo-cafe"),
            "용산", StopCategory.Cafe, listOf("카페", "한강", "여유"),
        ),
        SeedPoi(
            Stop("seongsu-pop", "성수 팝업 거리 (데모)", "서울 성동구 연무장길 인근", 37.54420, 127.05560, "peyya-demo-seongsu"),
            "성수", StopCategory.Shopping, listOf("성수", "쇼핑", "감성"),
        ),
        SeedPoi(
            Stop("seongsu-brunch", "성수 브런치 키친 (데모)", "서울 성동구 서울숲2길 인근", 37.54480, 127.04290, "peyya-demo-brunch"),
            "성수", StopCategory.Food, listOf("성수", "브런치", "맛집"),
        ),
        SeedPoi(
            Stop("bundang-park", "중앙공원 피크닉 존 (데모)", "경기 성남시 분당구 수내동", 37.37890, 127.11450, "peyya-demo-park"),
            "분당", StopCategory.Park, listOf("분당", "공원", "가족", "야외"),
        ),
        SeedPoi(
            Stop("bundang-bbq", "분당 가족 고깃집 (데모)", "경기 성남시 분당구 정자동 인근", 37.36540, 127.10820, "peyya-demo-bbq"),
            "분당", StopCategory.Food, listOf("분당", "고기", "저녁", "가족"),
        ),
    )

    fun communitySeed(now: Long = System.currentTimeMillis()): List<CommunityRoute> = listOf(
        CommunityRoute(
            id = "comm-yongsan",
            region = "용산",
            title = "박물관 반나절 + 한식 마무리",
            stops = listOf(pois[0].stop, pois[1].stop),
            authorLabel = "지우맘",
            likes = 128,
            likedByMe = false,
            satisfactionNote = "아이도 지루하지 않았고, 주차 동선이 편했어요.",
            comments = listOf(
                CommunityComment(
                    id = "c1",
                    authorLabel = "주차왕",
                    body = "박물관 주차 여유 있을 때 가기 좋아요.",
                    createdAtEpochMs = now - 86_400_000,
                    imageModel = PeyyaImages.MUSEUM,
                ),
                CommunityComment(
                    id = "c2",
                    authorLabel = "아빠운전",
                    body = "시동 걸고 다음 맛집까지 자연스러웠습니다.",
                    createdAtEpochMs = now - 43_200_000,
                    imageModel = PeyyaImages.FOOD,
                ),
            ),
            heroTone = StopCategory.Culture,
            reviewImageModel = PeyyaImages.MUSEUM,
        ),
        CommunityRoute(
            id = "comm-seongsu",
            region = "성수",
            title = "성수 감성 산책 · 브런치",
            stops = listOf(pois[3].stop, pois[4].stop),
            authorLabel = "주말러",
            likes = 86,
            likedByMe = false,
            satisfactionNote = "차 대고 걷기 좋은 구간만 골랐어요.",
            comments = listOf(
                CommunityComment(
                    id = "c3",
                    authorLabel = "성수동인",
                    body = "연무장길 주말 혼잡 주의!",
                    createdAtEpochMs = now - 172_800_000,
                    imageModel = PeyyaImages.CAFE,
                ),
            ),
            heroTone = StopCategory.Shopping,
            reviewImageModel = PeyyaImages.SEONGSU,
        ),
        CommunityRoute(
            id = "comm-bundang",
            region = "분당",
            title = "공원 피크닉 후 가족 고기",
            stops = listOf(pois[5].stop, pois[6].stop),
            authorLabel = "분당패밀리",
            likes = 64,
            likedByMe = false,
            satisfactionNote = "아이들 뛰어놀고 저녁까지 한 번에.",
            comments = emptyList(),
            heroTone = StopCategory.Park,
            reviewImageModel = PeyyaImages.PARK,
        ),
    )

    fun presetSaved(): SavedRoute = SavedRoute(
        id = PresetTrips.weekendMuseumTrip.id,
        title = PresetTrips.weekendMuseumTrip.title,
        stops = PresetTrips.weekendMuseumTrip.stops,
        status = RouteStatus.Scheduled,
        source = RouteSource.Preset,
        region = "용산",
        scheduledAtEpochMs = null,
        note = "공모전 데모 프리셋",
    )
}
