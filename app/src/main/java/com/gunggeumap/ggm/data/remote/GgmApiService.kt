package com.gunggeumap.ggm.data.remote

import com.gunggeumap.ggm.data.model.ApiResult
import com.gunggeumap.ggm.ui.viewmodel.dto.AnswerResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionDetailResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.ShortInfo
import retrofit2.http.GET
import retrofit2.http.Path

interface GgmApiService {
    @GET("api/questions/top")
    suspend fun getTopQuestions(): ApiResult<List<QuestionSummary>>

    @GET("api/short-infos")
    suspend fun getShortInfos(): ApiResult<List<ShortInfo>>

    @GET("api/questions/{id}")
    suspend fun getQuestionDetail(
        @Path("id") id: Long
    ): ApiResult<QuestionDetailResponse>

    @GET("api/questions/{id}/answers")
    suspend fun getAnswers(
        @Path("id") id: Long
    ): ApiResult<List<AnswerResponse>>
}
