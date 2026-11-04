package com.peyya.app.review

/**
 * 별점 1차 + 한글 키워드 보강으로 [Sentiment]를 산출한다. I/O 없는 순수 함수.
 *
 * - 4–5 → Positive, 3 → Neutral, 1–2 → Negative
 * - 부정 키워드만 → 한 단계 부정 쪽, 긍정만 → 한 단계 긍정 쪽
 * - 긍정·부정 동시 → 별점 1차 유지
 * - 빈 글 → 보강 없음 ([SentimentSource.Stars])
 */
object SentimentLabeler {

    private val negativeKeywords = listOf("실망", "별로", "최악", "비싸", "더럽")
    private val positiveKeywords = listOf("좋았", "최고", "다시", "추천", "만족")

    fun label(stars: Int, comment: String): Sentiment {
        require(stars in 1..5) { "stars must be in 1..5, was $stars" }

        val fromStars = labelFromStars(stars)
        val matchedNegative = matchKeywords(comment, negativeKeywords)
        val matchedPositive = matchKeywords(comment, positiveKeywords)
        val matched = matchedPositive + matchedNegative

        if (matched.isEmpty()) {
            return Sentiment(
                label = fromStars,
                source = SentimentSource.Stars,
                matchedKeywords = emptyList(),
            )
        }

        val hasNeg = matchedNegative.isNotEmpty()
        val hasPos = matchedPositive.isNotEmpty()
        val adjusted = when {
            hasNeg && hasPos -> fromStars
            hasNeg -> shiftNegative(fromStars)
            hasPos -> shiftPositive(fromStars)
            else -> fromStars
        }

        return Sentiment(
            label = adjusted,
            source = SentimentSource.StarsAndKeywords,
            matchedKeywords = matched,
        )
    }

    private fun labelFromStars(stars: Int): SentimentLabel =
        when (stars) {
            4, 5 -> SentimentLabel.Positive
            3 -> SentimentLabel.Neutral
            else -> SentimentLabel.Negative
        }

    private fun shiftNegative(label: SentimentLabel): SentimentLabel =
        when (label) {
            SentimentLabel.Positive -> SentimentLabel.Neutral
            SentimentLabel.Neutral -> SentimentLabel.Negative
            SentimentLabel.Negative -> SentimentLabel.Negative
        }

    private fun shiftPositive(label: SentimentLabel): SentimentLabel =
        when (label) {
            SentimentLabel.Negative -> SentimentLabel.Neutral
            SentimentLabel.Neutral -> SentimentLabel.Positive
            SentimentLabel.Positive -> SentimentLabel.Positive
        }

    private fun matchKeywords(comment: String, keywords: List<String>): List<String> {
        if (comment.isBlank()) return emptyList()
        return keywords.filter { keyword -> comment.contains(keyword) }
    }
}
