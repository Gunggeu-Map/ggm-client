package com.gunggeumap.ggm.ui.viewmodel.dto

import com.gunggeumap.ggm.answer.enums.VoteType
import java.time.LocalDateTime

data class AnswerResponse(
    val writer: String,
    val content: String,
    val createdAt: LocalDateTime,
    val likeCount: Long,
    val dislikeCount: Long,
    val isGpt: Boolean,
    val userVote: VoteType
)
