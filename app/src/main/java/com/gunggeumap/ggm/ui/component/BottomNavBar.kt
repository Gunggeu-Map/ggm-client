package com.gunggeumap.ggm.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "홈", Icons.Filled.Home, Icons.Outlined.Home)
    object Map : BottomNavItem("map", "지도", Icons.Filled.Map, Icons.Outlined.Map)
    object MyPage : BottomNavItem("mypage", "마이페이지", Icons.Filled.Person, Icons.Outlined.Person)

    companion object {
        val items = listOf(Home, Map, MyPage)
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar {
        BottomNavItem.items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}