package com.gunggeumap.ggm.data.remote.dto

data class QuestionRegisterRequest(
    val userId: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,   // 이미지 업로드 안 했으면 null
    val latitude: Float,
    val longitude: Float,
    val isPublic: Boolean
)