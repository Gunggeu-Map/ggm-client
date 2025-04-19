package com.gunggeumap.ggm.ui.model

import com.gunggeumap.ggm.answer.enums.VoteType
import java.time.LocalDateTime

data class QuestionDetailUiModel(
    val title: String,
    val category: String,
    val writerId: Long,
    val imgUrl: String?,
    val like: Int,
    val content: String,
    val aiAnswer: AnswerUiModel?,
    val answers: List<AnswerUiModel>
)

data class AnswerUiModel(
    val writer: String,
    val content: String,
    val createdAt: LocalDateTime,
    val like: Long,
    val dislike: Long,
    val isGpt: Boolean,
    val userVote: VoteType
)