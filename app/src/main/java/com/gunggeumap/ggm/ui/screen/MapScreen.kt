package com.gunggeumap.ggm.ui.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunggeumap.ggm.ui.component.TopBar
import com.gunggeumap.ggm.ui.permission.RequestLocationPermission
import com.gunggeumap.ggm.ui.permission.SettingsPermissionDialog

@Composable
fun MapScreen(
    onBackClick: () -> Unit = {}
) {
    var locationGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    RequestLocationPermission(
        onPermissionGranted = { locationGranted = true },
        onPermissionDenied = { locationGranted = false },
        onPermissionPermanentlyDenied = { showSettingsDialog = true }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "지도 화면", onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (locationGranted) "📍 위치 권한 허용됨" else "⚠️ 위치 권한 필요",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "지도 및 위치 기반 질문 목록을 표시합니다.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showSettingsDialog) {
        SettingsPermissionDialog(
            onDismiss = { showSettingsDialog = false },
            onGoToSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                (context as? Activity)?.startActivity(intent)
                showSettingsDialog = false
            }
        )
    }
}
