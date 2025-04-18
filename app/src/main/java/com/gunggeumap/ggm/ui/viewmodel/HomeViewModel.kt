package com.gunggeumap.ggm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 간단한 인기 질문 요약용 DTO
data class QuestionSummary(
    val id: Long,
    val title: String,
    val answerCount: Int,
    val likeCount: Int
)

// 짧은 과학 상식 DTO
data class ShortInfo(
    val id: Long,
    val content: String
)

class HomeViewModel : ViewModel() {

    private val _topQuestions = MutableStateFlow<List<QuestionSummary>>(emptyList())
    val topQuestions: StateFlow<List<QuestionSummary>> = _topQuestions

    private val _shortInfos = MutableStateFlow<List<ShortInfo>>(emptyList())
    val shortInfos: StateFlow<List<ShortInfo>> = _shortInfos

    init {
        fetchDummyData()
    }

    private fun fetchDummyData() {
        viewModelScope.launch {
            _topQuestions.value = listOf(
                QuestionSummary(1, "지구는 왜 자전을 할까?", 5, 32),
                QuestionSummary(2, "빛은 왜 직진할까?", 3, 24),
                QuestionSummary(3, "달의 뒷면은 왜 안 보일까?", 2, 19),
                QuestionSummary(4, "우주에는 끝이 있을까?", 6, 51),
                QuestionSummary(5, "물은 왜 100도에서 끓을까?", 1, 8),
            )

            _shortInfos.value = listOf(
                ShortInfo(1, "사람의 뇌는 전기를 사용해 정보를 전달해요."),
                ShortInfo(2, "소금물은 순수한 물보다 더 빨리 끓어요."),
                ShortInfo(3, "사람의 눈은 약 1000만 가지 색을 구별할 수 있어요."),
                ShortInfo(4, "벌은 8자를 그리며 춤을 춰서 방향을 알려줘요."),
                ShortInfo(5, "우주는 계속 팽창하고 있어요."),
            )
        }
    }
}
