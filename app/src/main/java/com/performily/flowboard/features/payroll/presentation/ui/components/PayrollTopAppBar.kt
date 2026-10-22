package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * Top bar of the payroll screens with a back arrow and the screen title (MA-67, MA-69).
 *
 * @param title title of the screen.
 * @param onBack called when the back arrow is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollTopAppBar(title: String, onBack: () -> Unit) {
    FlowboardTopBar(
        title = title,
        onBack = onBack
    )
}
