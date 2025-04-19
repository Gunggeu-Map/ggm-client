package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gunggeumap.ggm.data.model.Category
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionDetail

@Composable
fun QuestionBottomSheet(
    detail: MapQuestionDetail,
    questionId: Long,
    onNavigateToDetail: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                // 제목 앞에 Q. 표시
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Q.",
                        color = Color(0xFF2962FF),                     // 더 선명한 블루
                        fontSize = (MaterialTheme.typography.titleMedium.fontSize.value + 2).sp, // 기존보다 +2sp
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "#${Category.valueOf(detail.category).label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "좋아요 ${detail.likeCount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                detail.imgUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Question image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                onDismiss()
                onNavigateToDetail(questionId)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("상세 보기")
        }
    }
}
