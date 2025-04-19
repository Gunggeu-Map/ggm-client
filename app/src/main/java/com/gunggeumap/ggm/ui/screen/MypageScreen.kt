package com.gunggeumap.ggm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.ui.component.ProfileCard
import com.gunggeumap.ggm.ui.component.TopBar
import com.gunggeumap.ggm.ui.viewmodel.dto.QuestionSummary
import com.gunggeumap.ggm.ui.viewmodel.dto.UserMypage
import kotlinx.coroutines.launch

@Composable
fun MyPageScreen() {
    val coroutineScope = rememberCoroutineScope()

    var userInfo by remember { mutableStateOf<UserMypage?>(null) }
    var myQuestions by remember { mutableStateOf<List<QuestionSummary>>(emptyList()) }
    var myAnswers by remember { mutableStateOf<List<QuestionSummary>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userRes = ApiClient.api.getMyPageUserInfo()
            val questionRes = ApiClient.api.getMyQuestions()
            val answerRes = ApiClient.api.getMyAnsweredQuestions()

            if (userRes.success && userRes.data != null) userInfo = userRes.data
            if (questionRes.success && questionRes.data != null) myQuestions = questionRes.data
            if (answerRes.success && answerRes.data != null) myAnswers = answerRes.data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            title = "마이페이지",
            onNotificationClick = { /* TODO */ }
        )

        Spacer(Modifier.height(12.dp))

        userInfo?.let {
            ProfileCard(
                profileImageUrl = null,
                nickname = it.nickName,
                questionCount = it.questionCount.toInt(),
                answerCount = it.answerCount.toInt()
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                // 🔹 탭 영역 (약간 회색)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    ) {
                        Text(
                            text = "질문",
                            modifier = Modifier.padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    ) {
                        Text(
                            text = "답변",
                            modifier = Modifier.padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 🔹 리스트 영역 (배경 흰색)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    val listData = if (selectedTab == 0) myQuestions else myAnswers
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        itemsIndexed(listData) { index, item ->
                            QuestionAnswerRow(
                                title = item.title,
                                answerCount = item.answerCount,
                                likeCount = item.likeCount
                            )
                            if (index != listData.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionAnswerRow(
    title: String,
    answerCount: Int,
    likeCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "답변 $answerCount   좋아요 $likeCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
