package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Rounded card with a title, a text button on the right and free content below.
 *
 * Shared by the HR dashboard and the employee dashboard.
 *
 * @param title section title.
 * @param action label of the text button.
 * @param onAction called when the text button is tapped.
 * @param content content of the section.
 */
@Composable
internal fun DashboardSectionCard(
    title: String,
    action: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = Spacing.l, end = Spacing.s, top = Spacing.s, bottom = Spacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAction) { Text(action) }
            }
            Column(modifier = Modifier.padding(end = Spacing.s)) {
                content()
            }
        }
    }
}

/**
 * Short muted message shown in place of a section that has no content.
 *
 * @param text message to show.
 */
@Composable
internal fun DashboardSectionMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = Spacing.s)
    )
}

/**
 * Banner shown when nothing could be loaded, with a retry button.
 *
 * @param message explanation of the problem.
 * @param onRetry called when "Reintentar" is tapped.
 */
@Composable
internal fun DashboardErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.l, end = Spacing.s, top = Spacing.s, bottom = Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

/**
 * Amber banner that calls attention to something waiting for the user and opens it when tapped.
 *
 * @param text message of the banner.
 * @param onClick called when the banner is tapped.
 * @param icon icon shown at the start of the banner.
 */
@Composable
internal fun DashboardAlertBanner(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector = FlowboardIcons.Article
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = DashboardAccent.AMBER.container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DashboardAccent.AMBER.content,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DashboardAccent.AMBER.content,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = FlowboardIcons.ChevronRight,
                contentDescription = null,
                tint = DashboardAccent.AMBER.content
            )
        }
    }
}
