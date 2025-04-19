package com.gunggeumap.ggm.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF14213D),
            contentColor = Color.White
        ),
        shape = ButtonDefaults.shape,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Icon(
            imageVector = Icons.Outlined.Create,
            contentDescription = "질문하기",
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = "질문",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
