package com.gunggeumap.ggm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gunggeumap.ggm.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow<QuestionDetailState>(QuestionDetailState.Loading)
    val state: StateFlow<QuestionDetailState> = _state

    fun load(id: Long) {
        _state.value = QuestionDetailState.Loading
        viewModelScope.launch {
            QuestionRepository.fetchDetail(id)   // ← 그냥 호출
                .onSuccess { _state.value = QuestionDetailState.Success(it) }
                .onFailure { _state.value = QuestionDetailState.Error(it.message ?: "오류") }
        }
    }
}