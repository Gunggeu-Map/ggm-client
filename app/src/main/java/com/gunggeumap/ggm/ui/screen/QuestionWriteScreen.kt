package com.gunggeumap.ggm.ui.screen

import android.net.Uri
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

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gunggeumap.ggm.ui.component.TopBar

/**
 * 질문 작성 화면 (onBackClick 하나만 유지)
 */
@Composable
fun QuestionWriteScreen(
    onBackClick: () -> Unit,
    onWriteComplete: () -> Unit = {}      // 작성 완료 후 동작이 필요하면 람다만 넘겨주세요
) {
    /* ---------- 상태 ---------- */
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isPublic by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    /* ---------- 이미지 선택 런처 ---------- */
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
        }

    /* ---------- UI ---------- */
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "질문 작성", onBackClick = onBackClick)

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {

            /* --- 이미지 첨부 박스 --- */
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
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Add",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("0 / 1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "selected",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* --- 공개/비공개 --- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isPublic, onClick = { isPublic = true })
                Text("공개하기")
                Spacer(Modifier.width(24.dp))
                RadioButton(selected = !isPublic, onClick = { isPublic = false })
                Text("비공개하기")
            }

            Spacer(Modifier.height(24.dp))

            /* --- 제목 --- */
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            /* --- 내용 --- */
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                placeholder = {
                    Text(
                        "글 내용\n\n욕설이나 비방, 성적 희롱 등은 차단될 수 있습니다.",
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(40.dp))

            /* --- 작성 완료 버튼 --- */
            Button(
                onClick = onWriteComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D183B))
            ) {
                Text("작성 완료", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
