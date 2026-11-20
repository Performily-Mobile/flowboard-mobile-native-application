package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardEmptyState
import com.performily.flowboard.core.designsystem.component.SkeletonList
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/** Placeholder of a list shown while a screen loads. */
@Composable
fun WellbeingLoading(modifier: Modifier = Modifier) {
    SkeletonList(modifier = modifier.fillMaxSize())
}

/** Centered message for errors or empty lists, with an optional action. */
@Composable
fun WellbeingMessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    FlowboardEmptyState(
        title = title,
        message = message,
        icon = if (actionLabel == "Reintentar") FlowboardIcons.Warning else FlowboardIcons.Favorite,
        modifier = modifier,
        action = if (actionLabel != null) {
            { OutlinedButton(onClick = onAction) { Text(actionLabel) } }
        } else {
            null
        }
    )
}
