package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/**
 * Top bar of the payroll screens with a back arrow and the screen title (MA-67, MA-69).
 *
 * @param title title of the screen.
 * @param onBack called when the back arrow is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollTopAppBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
