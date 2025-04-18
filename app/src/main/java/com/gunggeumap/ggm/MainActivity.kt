package com.gunggeumap.ggm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gunggeumap.ggm.ui.MainScreen
import com.gunggeumap.ggm.ui.theme.GgmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GgmTheme {
                MainScreen()
            }
        }
    }
}
