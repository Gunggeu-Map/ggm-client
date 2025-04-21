package com.gunggeumap.ggm.ui.screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.gunggeumap.ggm.ui.component.TopBar
import com.gunggeumap.ggm.ui.viewmodel.QuestionWriteViewModel
import kotlinx.coroutines.launch

@Composable
fun QuestionWriteScreen(
    onBackClick: () -> Unit,
    userId: Long = 1L,
    viewModel: QuestionWriteViewModel = viewModel()
) {
    /* ---- 입력 상태 ---- */
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isPublic by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    /* ---- 위치 상태 ---- */
    var latitude by remember { mutableStateOf<Float?>(null) }
    var longitude by remember { mutableStateOf<Float?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 위치 권한 확인
    val locationPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latitude = it.latitude.toFloat()
                    longitude = it.longitude.toFloat()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latitude = it.latitude.toFloat()
                    longitude = it.longitude.toFloat()
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* ---- ViewModel 상태 ---- */
    val loading by viewModel.loading.collectAsState()
    val result by viewModel.result.collectAsState()

    /* ---- Snackbar & CoroutineScope ---- */
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(result) {
        result?.onSuccess {
            Toast.makeText(context, "질문이 등록되었습니다.", Toast.LENGTH_SHORT).show()
            onBackClick()
            viewModel.reset()
        }?.onFailure { e ->
            snackbarHostState.showSnackbar("등록 실패: ${e.message ?: "알 수 없음"}")
            viewModel.reset()
        }
    }

    /* ---- UI ---- */
    Scaffold(
        topBar = { TopBar(title = "질문 작성", onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            /* 이미지 첨부 */
            val pickImageLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    imageUri = uri
                }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { if (imageUri == null) pickImageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(48.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("0 / 1", fontSize = 12.sp)
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* 공개 여부 */
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isPublic, onClick = { isPublic = true })
                Text("공개하기")
                Spacer(Modifier.width(24.dp))
                RadioButton(selected = !isPublic, onClick = { isPublic = false })
                Text("비공개하기")
            }

            Spacer(Modifier.height(24.dp))

            /* 제목 */
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            /* 내용 */
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(40.dp))

            /* 작성 완료 */
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("제목과 내용을 입력하세요.")
                        }
                        return@Button
                    }
                    if (latitude == null || longitude == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("위치 정보를 불러오는 중입니다.")
                        }
                        return@Button
                    }

                    viewModel.postQuestion(
                        userId, title, content,
                        imageUrl = null,
                        lat = latitude!!, lng = longitude!!,
                        isPublic = isPublic
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
                else Text("작성 완료", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
