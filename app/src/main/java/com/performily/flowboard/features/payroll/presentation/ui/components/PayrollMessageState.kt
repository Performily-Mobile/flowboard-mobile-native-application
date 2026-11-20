package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardEmptyState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/**
 * Centered message for an empty list or a failed load, with an optional action.
 *
 * @param title short headline.
 * @param message explanation shown below the headline.
 * @param modifier modifier applied to the message.
 * @param actionLabel text of the action button, or null for no button.
 * @param onAction called when the button is tapped.
 */
@Composable
fun PayrollMessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    FlowboardEmptyState(
        title = title,
        message = message,
        icon = if (actionLabel == "Reintentar") FlowboardIcons.Warning else FlowboardIcons.CreditCard,
        modifier = modifier.fillMaxSize(),
        action = if (actionLabel != null) {
            { OutlinedButton(onClick = onAction) { Text(actionLabel) } }
        } else {
            null
        }
    )
}
