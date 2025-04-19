package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val emoji = when (label) {
        "자연" -> "🌿"
        "우주" -> "🪐"
        "기술" -> "💻"
        "인체" -> "🧠"
        "환경" -> "🌎"
        "물리" -> "⚛️"
        "화학" -> "🧪"
        "생물" -> "🦠"
        "지구과학" -> "🌋"
        "일상 속 궁금증" -> "🤔"
        "기타" -> "❓"
        else -> "📌"
    }

    Surface(
        shape = RoundedCornerShape(50),
        shadowElevation = 4.dp,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color.Black
            )
        }
    }
}
