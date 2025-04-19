package com.gunggeumap.ggm.ui.viewmodel.dto

data class MapQuestionSummary(
    val id: Long,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val answerCount: Int,
    val likeCount: Int,
    val imageUrl: String?
)
