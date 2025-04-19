package com.gunggeumap.ggm.data.mapper

import com.gunggeumap.ggm.ui.model.AnswerUiModel
import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel
import com.gunggeumap.ggm.ui.viewmodel.dto.AnswerResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionDetailResponse

object QuestionMapper {

    fun toUi(
        q: QuestionDetailResponse,
        a: List<AnswerResponse>
    ): QuestionDetailUiModel = QuestionDetailUiModel(
        title = q.title,
        category = q.category,
        writerId = q.writerId,
        imgUrl = q.imgUrl,
        like = q.likeCount,
        content = q.content,
        answers = a.map { ar ->
            AnswerUiModel(
                writer = ar.writer,
                content = ar.content,
                createdAt = ar.createdAt,
                like = ar.likeCount,
                dislike = ar.dislikeCount,
                isGpt = ar.isGpt,
                userVote = ar.userVote
            )
        }
    )
}