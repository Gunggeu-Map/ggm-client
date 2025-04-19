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
import com.gunggeumap.ggm.ui.screen.HomeScreen
import com.gunggeumap.ggm.ui.screen.MapScreen
import com.gunggeumap.ggm.ui.screen.QuestionDetailScreen
import com.gunggeumap.ggm.ui.screen.QuestionWriteScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = { selectedRoute ->
                    if (selectedRoute != currentRoute) {
                        // ✅ 질문 작성 화면이 쌓여 있다면 pop 시켜서 제거
                        navController.popBackStack("questionWrite", inclusive = true)

                        navController.navigate(selectedRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
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
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToDetail = { questionId ->
                        navController.navigate("questionDetail/$questionId")
                    },
                    onNavigateToWrite = {
                        navController.navigate("questionWrite")
                    }
                )
            }

            composable(BottomNavItem.Map.route) {
                MapScreen(
                    onQuestionClick = {
                        navController.navigate("questionWrite")
                    }
                )
            }

            composable(BottomNavItem.MyPage.route) {
                MyPageScreen()
            }

            composable("questionDetail/{questionId}") { backStackEntry ->
                val questionId = backStackEntry.arguments?.getString("questionId")?.toLongOrNull()
                questionId?.let {
                    QuestionDetailScreen(
                        questionId = it,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable("questionWrite") {
                QuestionWriteScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MyPageScreen() {
    // TODO: 마이페이지 구성
}
