package com.peyya.app.data

import com.peyya.app.domain.Stop

/**
 * 가는 길(내비 중)에 보여줄 목적지 정보.
 * Stop에 직접 값이 있으면 그걸 쓰고, 없으면 이름·id로 시연용 팁을 채운다.
 */
object DestinationTipsCatalog {
    data class Tips(
        val highlights: List<String> = emptyList(),
        val parkingTips: List<String> = emptyList(),
        val familyTips: List<String> = emptyList(),
        /** 휠체어 엘리베이터 · 유모차 보관소 등 시설 뱃지 */
        val accessFacilities: List<String> = emptyList(),
    ) {
        fun isNotEmpty(): Boolean =
            highlights.isNotEmpty() ||
                parkingTips.isNotEmpty() ||
                familyTips.isNotEmpty() ||
                accessFacilities.isNotEmpty()
    }

    fun resolve(stop: Stop): Tips {
        if (stop.hasOnTheWayTips()) {
            val base = byId[stop.id]
                ?: byId[stop.id.removeSuffix("-second").removeSuffix("-first")]
                ?: matchByName(stop.name)
            return Tips(
                highlights = stop.highlights,
                parkingTips = stop.parkingTips,
                familyTips = stop.familyTips,
                accessFacilities = base?.accessFacilities.orEmpty(),
            )
        }
        return byId[stop.id]
            ?: byId[stop.id.removeSuffix("-second").removeSuffix("-first")]
            ?: matchByName(stop.name)
            ?: genericTips(stop.name)
    }

    val museumTips = Tips(
        highlights = listOf(
            "상설전시실 동선은 1층 로비 안내 키오스크에서 확인",
            "특별전은 시간대 입장일 수 있어요 · 예매 확인증 준비",
            "야외 열린마당·연못 쪽에서 가족 사진 스팟이 많아요",
        ),
        parkingTips = listOf(
            "주차장 입구는 서빙고로 쪽 · 주말엔 입차 대기 있을 수 있어요",
            "전기차 충전면은 B1 일부 · 만차 시 용산역 공영 대안",
            "하차 동선: 로비 엘리베이터 → 안내데스크에서 시설 안내",
        ),
        familyTips = listOf(
            "어린이박물관은 별도 예매 · 시간대 입장 확인",
            "시니어·유모차 동반 시 로비→전시실 엘리베이터 동선 추천",
            "유모차 보관소는 1층 안내데스크 옆 · 수유실은 같은 구역",
            "휠체어는 안내데스크 대여 · 전 층 휠체어 엘리베이터 이용 가능",
        ),
        accessFacilities = listOf(
            "휠체어 엘리베이터",
            "유모차 보관소",
            "휠체어 대여",
            "수유실",
        ),
    )

    val restaurantTips = Tips(
        highlights = listOf(
            "대표 메뉴는 한정식·국밥 · 대기 짧을 때 방문 추천",
            "창가 자리는 가족 사진용으로 인기",
        ),
        parkingTips = listOf(
            "매장 앞 단기 주차 · 만차 시 용산역 공영주차장 이용",
            "내비 도착 후 ‘용산 한식 담소 입구’ 하차 지점 확인",
        ),
        familyTips = listOf(
            "아기의자·유모차 출입 가능 · 키즈 식기 요청 가능",
            "입구 경사로 있음 · 엘리베이터는 건물 공용 홀 이용",
        ),
        accessFacilities = listOf(
            "유모차 출입",
            "아기의자",
            "경사로",
        ),
    )

    private val byId: Map<String, Tips> = mapOf(
        "museum" to museumTips,
        "restaurant" to restaurantTips,
    )

    private fun matchByName(name: String): Tips? = when {
        name.contains("박물관") -> byId.getValue("museum")
        name.contains("담소") || name.contains("한식") || name.contains("맛집") || name.contains("식당") ->
            byId.getValue("restaurant")
        else -> null
    }

    private fun genericTips(name: String): Tips = Tips(
        highlights = listOf(
            "도착 전 영업시간·휴무일을 한 번 더 확인해 주세요",
            "입구·안내 데스크 위치를 내비 도착 안내와 맞춰 보세요",
        ),
        parkingTips = listOf(
            "주변 공영·건물 주차 가능 여부를 도착 10분 전 확인",
            "하차 후 도보 동선이 있으면 유모차·시니어 동선을 우선하세요",
        ),
        familyTips = emptyList(),
    )
}
