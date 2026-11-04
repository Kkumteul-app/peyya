package com.peyya.app.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentimentLabelerTest {

    @Test
    fun stars5_emptyComment_isPositiveStars() {
        val result = SentimentLabeler.label(5, "")
        assertEquals(SentimentLabel.Positive, result.label)
        assertEquals(SentimentSource.Stars, result.source)
        assertEquals(emptyList<String>(), result.matchedKeywords)
    }

    @Test
    fun stars4_blankComment_isPositiveStars() {
        val result = SentimentLabeler.label(4, "   ")
        assertEquals(SentimentLabel.Positive, result.label)
        assertEquals(SentimentSource.Stars, result.source)
    }

    @Test
    fun stars3_emptyComment_isNeutralStars() {
        val result = SentimentLabeler.label(3, "")
        assertEquals(SentimentLabel.Neutral, result.label)
        assertEquals(SentimentSource.Stars, result.source)
    }

    @Test
    fun stars1_emptyComment_isNegativeStars() {
        val result = SentimentLabeler.label(1, "")
        assertEquals(SentimentLabel.Negative, result.label)
        assertEquals(SentimentSource.Stars, result.source)
    }

    @Test
    fun stars2_emptyComment_isNegativeStars() {
        val result = SentimentLabeler.label(2, "")
        assertEquals(SentimentLabel.Negative, result.label)
        assertEquals(SentimentSource.Stars, result.source)
    }

    @Test
    fun stars5_negativeKeyword_shiftsToNeutral() {
        val result = SentimentLabeler.label(5, "기대했는데 실망했어요")
        assertEquals(SentimentLabel.Neutral, result.label)
        assertEquals(SentimentSource.StarsAndKeywords, result.source)
        assertTrue(result.matchedKeywords.contains("실망"))
    }

    @Test
    fun stars1_positiveKeyword_shiftsToNeutral() {
        val result = SentimentLabeler.label(1, "다시 가고 싶진 않지만")
        assertEquals(SentimentLabel.Neutral, result.label)
        assertEquals(SentimentSource.StarsAndKeywords, result.source)
        assertTrue(result.matchedKeywords.contains("다시"))
    }

    @Test
    fun stars3_positiveKeyword_shiftsToPositive() {
        val result = SentimentLabeler.label(3, "만족스러웠음")
        assertEquals(SentimentLabel.Positive, result.label)
        assertEquals(SentimentSource.StarsAndKeywords, result.source)
        assertTrue(result.matchedKeywords.contains("만족"))
    }

    @Test
    fun bothPolarityKeywords_keepsStarLabel() {
        val result = SentimentLabeler.label(5, "최고인데 별로였음")
        assertEquals(SentimentLabel.Positive, result.label)
        assertEquals(SentimentSource.StarsAndKeywords, result.source)
        assertTrue(result.matchedKeywords.contains("최고"))
        assertTrue(result.matchedKeywords.contains("별로"))
    }
}
