package com.peyya.app.ui

/**
 * Phone 하단 탭 4개 — 계획(핵심) / 실행 / 추천 / 마이.
 */
enum class PhoneTab(
    val label: String,
    val title: String,
) {
    Plan(label = "계획", title = "계획"),
    Execute(label = "실행", title = "실행"),
    Recommend(label = "추천", title = "추천"),
    My(label = "마이", title = "마이"),
}
