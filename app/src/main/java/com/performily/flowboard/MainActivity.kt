package com.performily.flowboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.performily.flowboard.core.designsystem.theme.FlowboardTheme
import com.performily.flowboard.core.navigation.FlowboardApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowboardTheme {
                FlowboardApp()
            }
        }
    }
}
