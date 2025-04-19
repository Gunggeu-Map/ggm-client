package com.gunggeumap.ggm.ui.viewmodel.dto

data class QuestionDetailResponse(
    val title: String,
    val category: String,
    val writerId: Long,
    val imgUrl: String?,
    val likeCount: Int,
    val content: String
)
