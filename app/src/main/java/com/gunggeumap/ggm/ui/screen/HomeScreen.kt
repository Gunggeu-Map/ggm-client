package com.gunggeumap.ggm.ui.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gunggeumap.ggm.ui.component.QuestionButton
import com.gunggeumap.ggm.ui.component.TopBar
import com.gunggeumap.ggm.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToWrite: () -> Unit = {},
    onNotificationClick: () -> Unit = {}          // 🔔 받는 람다
) {
    /* --------------------------------------------------------------------- */
    /* 상태 */
    val context = LocalContext.current
    val topQuestions by viewModel.topQuestions.collectAsState()
    val shortInfos   by viewModel.shortInfos.collectAsState()
    val randomShortInfo = remember(shortInfos) { shortInfos.randomOrNull() }

    /* 첫 진입 시 데이터 로딩 */
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "LaunchedEffect 실행")
        viewModel.fetchTopQuestions()
        viewModel.cacheShortInfosIfNeeded()
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    /* --------------------------------------------------------------------- */
    /* 위치 권한 런처 */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) onNavigateToWrite() else
            Log.w("HomeScreen", "위치 권한 거부")
    }

    /* --------------------------------------------------------------------- */
    /* UI */
    Box(Modifier.fillMaxSize()) {

        /* 메인 내용 */
        Column(Modifier.fillMaxSize()) {

            /* TopBar */
            TopBar(
                title = "홈",
                onNotificationClick = onNotificationClick   // 여기!!
            )

            Spacer(Modifier.height(16.dp))

            /* 오늘의 질문 TOP5 ------------------------------------------------ */
            Text(
                "🔥 오늘의 질문 TOP 5",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            topQuestions.forEachIndexed { index, q ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetail(q.id) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                q.title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "답변 ${q.answerCount}  좋아요 ${q.likeCount}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(Modifier.padding(start = 40.dp))
                }
            }

            /* 짧과 ------------------------------------------------------------- */
            Spacer(Modifier.height(28.dp))

            Text(
                "💡 짧.과 : 짧은 과학 상식",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            randomShortInfo?.let {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) {
                    Box(Modifier.padding(20.dp)) {
                        Text(
                            it.content,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        /* 질문 작성 Floating 버튼 ------------------------------------------- */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            QuestionButton(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {
                        onNavigateToWrite()
                    } else {
                        val activity = context as? Activity
                        val showRationale = activity?.let {
                            androidx.core.app.ActivityCompat
                                .shouldShowRequestPermissionRationale(
                                    it, Manifest.permission.ACCESS_FINE_LOCATION
                                )
                        } ?: true

                        if (!showRationale) {
                            showPermissionDialog = true
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                }
            )
        }

        /* 위치 권한 거절 다이얼로그 ------------------------------------------ */
        if (showPermissionDialog) {
            val activity = context as? Activity
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("위치 권한이 필요합니다") },
                text = {
                    Text("질문을 등록하려면 위치 권한이 필요합니다.\n설정에서 권한을 허용해 주세요.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            activity?.startActivity(intent)
                        }
                    ) { Text("설정으로 이동") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPermissionDialog = false }
                    ) { Text("닫기") }
                }
            )
        }
    }
}
