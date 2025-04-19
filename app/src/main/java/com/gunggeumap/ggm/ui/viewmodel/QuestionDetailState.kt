package com.gunggeumap.ggm.ui.viewmodel

import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel

sealed class QuestionDetailState {
    object Loading : QuestionDetailState()
    data class Success(val data: QuestionDetailUiModel) : QuestionDetailState()
    data class Error(val message: String) : QuestionDetailState()
}