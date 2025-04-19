package com.gunggeumap.ggm.data.repository

import com.gunggeumap.ggm.data.mapper.QuestionMapper
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel

object QuestionRepository {
    private val api = ApiClient.api

    suspend fun fetchDetail(id: Long): Result<QuestionDetailUiModel> =
        runCatching {
            val q = api.getQuestionDetail(id)
            val a = api.getAnswers(id)
            require(q.success && a.success)
            QuestionMapper.toUi(q.data!!, a.data!!)
        }
}