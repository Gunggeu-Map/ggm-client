package com.gunggeumap.ggm.data.remote

import com.gunggeumap.ggm.data.model.ApiResult
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.ShortInfo
import retrofit2.http.GET

interface GgmApiService {
    @GET("api/questions/top")
    suspend fun getTopQuestions(): ApiResult<List<QuestionSummary>>

    @GET("api/short-infos")
    suspend fun getShortInfos(): ApiResult<List<ShortInfo>>
}
