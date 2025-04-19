package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionSummary

@Composable
fun SearchResultBottomSheet(
    results: List<MapQuestionSummary>,
    onItemClick: (MapQuestionSummary) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(top = 4.dp)) {
        items(results) { item ->
            QuestionCard(
                item = item,
                modifier = Modifier.clickable { onItemClick(item) }
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}
