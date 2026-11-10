package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.performily.flowboard.core.designsystem.theme.Spacing

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = Spacing.s)) {
                Text(actionLabel)
            }
        }
    }
}
