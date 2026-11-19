package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardEmptyState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/**
 * Empty state of the attendance screens, drawn with the shared [FlowboardEmptyState].
 *
 * @param title main line.
 * @param message explanation below the title.
 * @param actionLabel text of the action button, or null to hide it.
 * @param onAction called when the action button is tapped.
 * @param modifier modifier applied to the state.
 */
@Composable
fun AttendanceEmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FlowboardEmptyState(
        title = title,
        message = message,
        icon = FlowboardIcons.Calendar,
        modifier = modifier,
        action = if (actionLabel != null && onAction != null) {
            { OutlinedButton(onClick = onAction) { Text(actionLabel) } }
        } else {
            null
        }
    )
}
