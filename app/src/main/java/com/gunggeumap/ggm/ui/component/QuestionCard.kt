package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionSummary

@Composable
fun QuestionCard(
    item: MapQuestionSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Q.",
                    color = Color(0xFF2962FF),
                    fontSize = (MaterialTheme.typography.titleMedium.fontSize.value + 2).sp,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "답변 ${item.answerCount}   좋아요 ${item.likeCount}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            item.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
