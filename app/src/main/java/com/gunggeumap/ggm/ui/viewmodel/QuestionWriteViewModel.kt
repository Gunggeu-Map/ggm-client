package com.gunggeumap.ggm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.data.remote.dto.QuestionRegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class QuestionWriteViewModel : ViewModel() {

    private val api = ApiClient.api

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _result = MutableStateFlow<Result<Unit>?>(null)
    val result: StateFlow<Result<Unit>?> = _result

    fun postQuestion(
        userId: Long,
        title: String,
        content: String,
        imageUrl: String?,
        lat: Float,
        lng: Float,
        isPublic: Boolean
    ) = viewModelScope.launch {
        _loading.value = true
        try {
            val body = QuestionRegisterRequest(
                userId, title, content, imageUrl, lat, lng, isPublic
            )
            val res = api.registerQuestion(body)            // Response<Void>
            if (res.isSuccessful) _result.value = Result.success(Unit)
            else _result.value = Result.failure(HttpException(res))
        } catch (e: IOException) {                          // 네트워크 오류
            _result.value = Result.failure(e)
        } finally {
            _loading.value = false
        }
    }

    fun reset() { _result.value = null }
}
