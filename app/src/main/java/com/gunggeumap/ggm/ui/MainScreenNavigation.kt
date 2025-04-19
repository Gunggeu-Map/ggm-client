package com.gunggeumap.ggm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gunggeumap.ggm.ui.component.BottomNavBar
import com.gunggeumap.ggm.ui.component.BottomNavItem
import com.gunggeumap.ggm.ui.screen.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = { selected ->
                    if (selected != currentRoute) {
                        // 질문 작성 화면, 알림 화면이 스택에 있으면 정리
                        navController.popBackStack("questionWrite", inclusive = true)
                        navController.popBackStack("alarm", inclusive = true)

                        navController.navigate(selected) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            /* ─── 홈 탭 ─── */
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToDetail = { id -> navController.navigate("questionDetail/$id") },
                    onNavigateToWrite  = { navController.navigate("questionWrite") },
                    onNotificationClick = { navController.navigate("alarm") }
                )
            }

            /* ─── 지도 탭 ─── */
            composable(BottomNavItem.Map.route) {
                MapScreen(
                    onQuestionClick = { navController.navigate("questionWrite") }
                )
            }

            /* ─── 마이페이지 탭 ─── */
            composable(BottomNavItem.MyPage.route) { MyPageScreen() }

            /* ─── 질문 상세 ─── */
            composable("questionDetail/{questionId}") { backStack ->
                backStack.arguments?.getString("questionId")?.toLongOrNull()?.let { id ->
                    QuestionDetailScreen(
                        questionId = id,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            /* ─── 질문 작성 ─── */
            composable("questionWrite") {
                QuestionWriteScreen(onBackClick = { navController.popBackStack() })
            }

            /* ─── 알림 ─── */
            composable("alarm") {
                AlarmScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
