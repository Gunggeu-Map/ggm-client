package com.gunggeumap.ggm.data.remote

import com.gunggeumap.ggm.data.model.ApiResult
import com.gunggeumap.ggm.ui.viewmodel.dto.AnswerResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionDetailResponse
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionDetail
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.ShortInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GgmApiService {
    @GET("api/questions/top")
    suspend fun getTopQuestions(): ApiResult<List<QuestionSummary>>

    @GET("api/short-infos")
    suspend fun getShortInfos(): ApiResult<List<ShortInfo>>

    @GET("api/questions/map")
    suspend fun getQuestionsInMapBounds(
        @Query("swLat") swLat: Double,
        @Query("swLng") swLng: Double,
        @Query("neLat") neLat: Double,
        @Query("neLng") neLng: Double
    ): ApiResult<List<MapQuestionSummary>>

    @GET("api/questions/{id}")
    suspend fun getQuestionDetail(@Path("id") id: Long): ApiResult<MapQuestionDetail>

    @GET("api/questions/map/search")
    suspend fun searchQuestionsByKeyword(
        @Query("keyword") keyword: String
    ): ApiResult<List<MapQuestionSummary>>

    @GET("api/questions/map/search/category")
    suspend fun searchQuestionsByCategory(
        @Query("category") category: String
    ): ApiResult<List<MapQuestionSummary>>

    @GET("api/questions/{id}/answers")
    suspend fun getAnswers(
        @Path("id") id: Long
    ): ApiResult<List<AnswerResponse>>
}
