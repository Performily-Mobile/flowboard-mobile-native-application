package com.performily.flowboard.features.notifications.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.AvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.core.designsystem.theme.PendingContainer
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.theme.SuspendedContainer
import com.performily.flowboard.features.notifications.domain.entity.AppNotification
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationCategory

/**
 * One notice: colored icon of its source, title, detail and time. An unread notice has a tinted
 * background, a bold title and a dot.
 *
 * @param notification notice to display.
 * @param time already formatted moment of the notice.
 * @param onClick called when the notice is tapped.
 * @param modifier modifier applied to the row.
 */
@Composable
fun NotificationItem(
    notification: AppNotification,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val unread = !notification.isRead
    Surface(
        shape = shape,
        color = if (unread) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = UNREAD_ALPHA)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(notification.category.container(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.category.icon(),
                    contentDescription = null,
                    tint = OnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (unread) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

/** Background of the icon of each source. */
private fun NotificationCategory.container(): Color = when (this) {
    NotificationCategory.REQUEST -> AvatarContainer
    NotificationCategory.APPROVAL -> PendingContainer
    NotificationCategory.PAYSLIP -> ActiveContainer
    NotificationCategory.BENEFIT -> SuspendedContainer
}

/** Icon of each source. */
private fun NotificationCategory.icon(): ImageVector = when (this) {
    NotificationCategory.REQUEST -> FlowboardIcons.Article
    NotificationCategory.APPROVAL -> FlowboardIcons.Group
    NotificationCategory.PAYSLIP -> FlowboardIcons.CreditCard
    NotificationCategory.BENEFIT -> FlowboardIcons.Gift
}

/** Opacity of the tint of an unread notice. */
private const val UNREAD_ALPHA = 0.45f
