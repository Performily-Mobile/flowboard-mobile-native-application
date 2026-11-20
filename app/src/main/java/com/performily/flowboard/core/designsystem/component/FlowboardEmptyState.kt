package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Message shown in place of a list or a block that has nothing to show yet.
 *
 * It explains what would appear there and, when there is one, offers the next step, so an empty
 * screen never looks broken.
 *
 * @param title short sentence that says the list is empty.
 * @param modifier modifier applied to the message.
 * @param icon optional icon that represents the content of the list.
 * @param message optional explanation or hint below the title.
 * @param action optional button with the next step.
 */
@Composable
fun FlowboardEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    message: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        if (icon != null) {
            EmptyStateIllustration(icon = icon)
            Spacer(Modifier.height(Spacing.s))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (action != null) {
            Spacer(Modifier.height(Spacing.s))
            action()
        }
    }
}

/**
 * Small illustration drawn with shapes of the theme: the icon on a tonal circle, two soft rings
 * around it and a few floating dots, so an empty screen looks intentional instead of blank while
 * keeping the colors of the light and dark themes.
 *
 * @param icon icon in the center of the illustration.
 */
@Composable
private fun EmptyStateIllustration(icon: ImageVector) {
    val ring = MaterialTheme.colorScheme.secondaryContainer
    val accent = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    Box(modifier = Modifier.size(ILLUSTRATION_SIZE), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = ring.copy(alpha = 0.35f), radius = size.minDimension / 2f, center = center)
            drawCircle(color = ring.copy(alpha = 0.6f), radius = size.minDimension * 0.36f, center = center)
            drawCircle(color = accent.copy(alpha = 0.5f), radius = 5.dp.toPx(), center = Offset(size.width * 0.16f, size.height * 0.30f))
            drawCircle(color = tertiary.copy(alpha = 0.45f), radius = 4.dp.toPx(), center = Offset(size.width * 0.86f, size.height * 0.22f))
            drawCircle(color = accent.copy(alpha = 0.3f), radius = 3.dp.toPx(), center = Offset(size.width * 0.80f, size.height * 0.84f))
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

/** Side of the empty-state illustration. */
private val ILLUSTRATION_SIZE = 132.dp
