package com.peyya.app.review

data class Sentiment(
    val label: SentimentLabel,
    val source: SentimentSource,
    val matchedKeywords: List<String> = emptyList(),
)
