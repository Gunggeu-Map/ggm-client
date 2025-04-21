package com.gunggeumap.ggm.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gunggeumap.ggm.ui.component.TopBar
import com.gunggeumap.ggm.ui.model.AnswerUiModel
import com.gunggeumap.ggm.ui.model.QuestionDetailUiModel
import com.gunggeumap.ggm.ui.viewmodel.QuestionDetailState
import com.gunggeumap.ggm.ui.viewmodel.QuestionDetailViewModel

/* ───────────────────────────────────────────────────────── */
/* 메인 컴포저블                                                */
/* ───────────────────────────────────────────────────────── */
@Composable
fun QuestionDetailScreen(
    questionId: Long,
    onBackClick: () -> Unit
) {
    val vm: QuestionDetailViewModel = viewModel()
    LaunchedEffect(questionId) { vm.load(questionId) }

    when (val s = vm.state.collectAsState().value) {
        is QuestionDetailState.Loading -> CenterLoader()
        is QuestionDetailState.Error   -> CenterError(s.message)
        is QuestionDetailState.Success -> DetailContentLazy(s.data, onBackClick)
    }
}

/* ───────────────────────────────────────────────────────── */
/* LazyColumn(헤더 + 답변 리스트) + 하단 입력창                      */
/* ───────────────────────────────────────────────────────── */
@Composable
private fun DetailContentLazy(
    data: QuestionDetailUiModel,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "질문 상세보기", onBackClick = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)            // 입력창 제외한 영역
                .padding(horizontal = 20.dp)
        ) {
            /* 헤더 카드 */
            item {
                QuestionCard(data)
                Spacer(Modifier.height(24.dp))
            }

            /* 답변 리스트 */
            items(data.answers) { answer ->
                AnswerItem(answer)
                Spacer(Modifier.height(16.dp))
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        BottomInputField()            // 하단 고정
    }
}

/* ───────────────────────────────────────────────────────── */
/* 질문 + AI 답변 카드                                          */
/* ───────────────────────────────────────────────────────── */
@Composable
private fun QuestionCard(data: QuestionDetailUiModel) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            /* 제목 */
            Text(data.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))

            /* 메타 정보 */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("카테고리 : ${data.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("작성자 : ${data.writerId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.ThumbUp, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("${data.like}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            /* 이미지 (있으면) */
            data.imgUrl?.let {
                Spacer(Modifier.height(20.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(20.dp))

            /* 본문 */
            Text(data.title, fontSize = 14.sp)

            /* AI 답변 */
            Spacer(Modifier.height(20.dp))
            Surface(
                shape  = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp)) {
                    Text("🤖", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI 답변", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = data.content ?: "AI 답변이 아직 없습니다.",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/* ───────────────────────────────────────────────────────── */
/* 답변 아이템                                                  */
/* ───────────────────────────────────────────────────────── */
@Composable
private fun AnswerItem(a: AnswerUiModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(a.writer, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ThumbUp, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(2.dp))
            Text("${a.like}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.ThumbDown, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(2.dp))
            Text("${a.dislike}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(a.createdAt.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(a.content, fontSize = 13.sp)
    }
}

/* ───────────────────────────────────────────────────────── */
/* 하단 칩형 입력창                                             */
/* ───────────────────────────────────────────────────────── */
/* ───────────────── BottomInputField (교체용) ───────────────── */
@Composable
private fun BottomInputField(
    onSend: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    Surface(
        shape  = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp) // 내부 여백
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null)

            Spacer(Modifier.width(8.dp))

            /* 기본 입력창(underline·cursor만) */
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            "답글 입력",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                    }
                }) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "전송")
            }
        }
    }
}


/* ───────────────────────────────────────────────────────── */
/* 로딩·에러 helper                                           */
/* ───────────────────────────────────────────────────────── */
@Composable private fun CenterLoader() =
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

@Composable private fun CenterError(msg: String) =
    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("에러: $msg") }
