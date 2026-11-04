package com.peyya.app.review

import org.junit.Assert.assertEquals
import org.junit.Test

class SentimentTest {

    @Test
    fun labels_coverPositiveNeutralNegative() {
        assertEquals(
            setOf("Positive", "Neutral", "Negative"),
            SentimentLabel.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun sources_coverStarsAndStarsAndKeywords() {
        assertEquals(
            setOf("Stars", "StarsAndKeywords"),
            SentimentSource.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun matchedKeywords_defaultEmpty() {
        val sentiment = Sentiment(
            label = SentimentLabel.Positive,
            source = SentimentSource.Stars,
        )
        assertEquals(emptyList<String>(), sentiment.matchedKeywords)
    }

    @Test
    fun matchedKeywords_preservesOrder() {
        val sentiment = Sentiment(
            label = SentimentLabel.Negative,
            source = SentimentSource.StarsAndKeywords,
            matchedKeywords = listOf("별로", "실망"),
        )
        assertEquals(listOf("별로", "실망"), sentiment.matchedKeywords)
    }
}
