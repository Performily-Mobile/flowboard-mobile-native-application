package com.performily.flowboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.performily.flowboard.core.designsystem.theme.FlowboardTheme
import com.performily.flowboard.core.navigation.FlowboardApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity of the app.
 *
 * It extends [FragmentActivity] because the biometric prompt of the login is hosted by a fragment.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
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
