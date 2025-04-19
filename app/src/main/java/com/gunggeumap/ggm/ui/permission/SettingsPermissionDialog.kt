package com.gunggeumap.ggm.ui.permission

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun SettingsPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("위치 권한이 필요합니다") },
        text = { Text("앱의 위치 기반 기능을 사용하려면 설정에서 권한을 허용해 주세요.") },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}
