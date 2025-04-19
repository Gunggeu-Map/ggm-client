package com.gunggeumap.ggm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.ShortInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

class HomeViewModel : ViewModel() {
    private val _topQuestions = MutableStateFlow<List<QuestionSummary>>(emptyList())
    val topQuestions: StateFlow<List<QuestionSummary>> = _topQuestions

    private val _shortInfos = MutableStateFlow<List<ShortInfo>>(emptyList())
    val shortInfos: StateFlow<List<ShortInfo>> = _shortInfos

    private var shortInfosCached = false

    fun fetchTopQuestions() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getTopQuestions()
                if (response.success && response.data != null) {
                    _topQuestions.value = response.data
                }
            } catch (e: IOException) {
                // 네트워크 오류 처리
            } catch (e: HttpException) {
                // HTTP 오류 처리
            }
        }
    }

    fun cacheShortInfosIfNeeded() {
        if (shortInfosCached) return

        viewModelScope.launch {
            try {
                val response = ApiClient.api.getShortInfos()
                if (response.success && response.data != null) {
                    _shortInfos.value = response.data
                    shortInfosCached = true
                }
            } catch (e: IOException) {
                // 네트워크 오류 처리
            } catch (e: HttpException) {
                // HTTP 오류 처리
            }
        }
    }
}
