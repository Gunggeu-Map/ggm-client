package com.gunggeumap.ggm.data.mapper

import com.gunggeumap.ggm.ui.model.AnswerUiModel
import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel
import com.gunggeumap.ggm.ui.viewmodel.dto.AnswerResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionDetailResponse

object QuestionMapper {

    fun toUi(
        q: QuestionDetailResponse,
        a: List<AnswerResponse>
    ): QuestionDetailUiModel {

        val gptAnswerDto = a.find { it.isGpt }
        val userAnswers  = a.filterNot { it.isGpt }

        return QuestionDetailUiModel(
            title     = q.title,
            category  = q.category,
            writerId  = q.writerId,
            imgUrl    = q.imgUrl,
            like      = q.likeCount,
            content   = q.content,
            aiAnswer  = gptAnswerDto?.let(::answerToUi ),
            answers   = userAnswers.map(::answerToUi)
        )
    }

    private fun answerToUi(dto: AnswerResponse) = AnswerUiModel(
        writer     = dto.writer,
        content    = dto.content,
        createdAt  = dto.createdAt,
        like       = dto.likeCount,
        dislike    = dto.dislikeCount,
        isGpt      = dto.isGpt,
        userVote   = dto.userVote
    )
}
