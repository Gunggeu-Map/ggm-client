// ── ui/component/SearchResultBottomSheet.kt
package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionSummary

@Composable
fun SearchResultBottomSheet(
    results: List<MapQuestionSummary>,
    onItemClick: (MapQuestionSummary) -> Unit
) {
    if (results.isEmpty()) {
        /* 🔹 결과 없음 */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)          // 시트가 너무 얇지 않도록
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "결과가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        /* 🔹 결과 리스트 */
        LazyColumn(modifier = Modifier.padding(top = 4.dp)) {
            items(results) { item ->
                QuestionCard(
                    item = item,
                    modifier = Modifier
                        .clickable { onItemClick(item) }
                        .fillMaxWidth()
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}
