package com.peyya.app.data

data class PlaceSuggestion(
    val label: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
)

object PlaceSearchCatalog {
    val suggestions: List<PlaceSuggestion> = listOf(
        PlaceSuggestion("서울 용산구 한강대로 405", "용산역·아이파크몰 인근", 37.5299, 126.9648),
        PlaceSuggestion("서울 용산구 서빙고로 137", "국립중앙박물관", 37.52386, 126.98047),
        PlaceSuggestion("서울 성동구 연무장길 5", "성수 카페거리", 37.54420, 127.05560),
        PlaceSuggestion("서울 성동구 서울숲2길 24", "서울숲역 인근", 37.54480, 127.04290),
        PlaceSuggestion("서울 강남구 테헤란로 152", "역삼역·회사 밀집", 37.5000, 127.0365),
        PlaceSuggestion("서울 서초구 서초대로 74길 11", "강남역 인근", 37.4979, 127.0276),
        PlaceSuggestion("서울 마포구 양화로 188", "합정·홍대", 37.5495, 126.9137),
        PlaceSuggestion("서울 송파구 올림픽로 300", "잠실·롯데월드타워", 37.5126, 127.1025),
        PlaceSuggestion("경기 성남시 분당구 정자일로 95", "정자역·회사", 37.3654, 127.1082),
        PlaceSuggestion("경기 성남시 분당구 수내동 55", "중앙공원", 37.37890, 127.11450),
        PlaceSuggestion("인천 연수구 센트럴로 263", "송도 국제업무지구", 37.3895, 126.6425),
        PlaceSuggestion("경기 하남시 미사강변대로 55", "미사역 인근", 37.5632, 127.1925),
    )

    fun search(query: String, limit: Int = 6): List<PlaceSuggestion> {
        val q = query.trim()
        if (q.isEmpty()) return suggestions.take(limit)
        return suggestions
            .filter {
                it.label.contains(q, ignoreCase = true) ||
                    it.detail.contains(q, ignoreCase = true)
            }
            .ifEmpty {
                suggestions.filter { s ->
                    q.length >= 2 && (s.label.contains(q.take(2)) || s.detail.contains(q.take(2)))
                }
            }
            .take(limit)
    }

    fun resolve(query: String): PlaceSuggestion? {
        val q = query.trim()
        if (q.isEmpty()) return null
        return suggestions.firstOrNull { it.label == q }
            ?: search(q, 1).firstOrNull()
    }

    val defaultCenter = PlaceSuggestion(
        label = "서울 중심",
        detail = "목적지를 검색하면 지도가 이동합니다",
        latitude = 37.5665,
        longitude = 126.9780,
    )
}
