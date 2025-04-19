package com.gunggeumap.ggm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunggeumap.ggm.ui.component.TopBar

@Composable
fun AlarmScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopBar(title = "알림", onBackClick = onBackClick) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = Color(0xFFB0B0B0)
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = "아직 알림이 없어요.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A8A8A)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "최근 알림을 이 곳에서 확인 할 수 있어요.",
                fontSize = 14.sp,
                color = Color(0xFF8A8A8A)
            )
        }
    }
}
