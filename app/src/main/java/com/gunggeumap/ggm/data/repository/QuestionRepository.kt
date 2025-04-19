package com.gunggeumap.ggm.data.repository

import com.gunggeumap.ggm.data.mapper.QuestionMapper
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.data.remote.GgmApiService
import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionRepository(
    private val api: GgmApiService = ApiClient.api
) {
    suspend fun fetchDetail(id: Long): Result<QuestionDetailUiModel> =
        runCatching {
            withContext(Dispatchers.IO) {
                val q = api.getQuestionDetail(id)
                val a = api.getAnswers(id)
                require(q.success && a.success)
                QuestionMapper.toUi(q.data!!, a.data!!)
            }
        }
}