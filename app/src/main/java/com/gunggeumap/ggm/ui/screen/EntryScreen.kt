package com.gunggeumap.ggm.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gunggeumap.ggm.ui.MainScreen

@Composable
fun EntryScreen() {
    // 하드코딩으로 true 설정 -> 바로 MainScreen 진입
    var isLoggedIn by remember { mutableStateOf(true) }

    if (isLoggedIn) {
        // 기존 MainScreen 파라미터 변경 없이 호출
        MainScreen()
    } else {
        LandingScreen(
            onKakaoLoginClick = { isLoggedIn = true /* TODO: 실제 로그인 처리 */ },
            onNaverLoginClick = { isLoggedIn = true /* TODO: 실제 로그인 처리 */ }
        )
    }
}