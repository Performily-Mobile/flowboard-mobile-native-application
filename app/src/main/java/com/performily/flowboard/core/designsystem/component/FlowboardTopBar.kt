package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Single title bar used by every screen of the app, so all titles share the same size, weight,
 * spacing and optional subtitle.
 *
 * The bar is flat, uses the screen background and keeps the title left aligned. A standard
 * 48 dp icon button leads the bar when [onBack] or [onClose] is given, as in Material 3 top app
 * bars; screens opened from the bottom bar simply omit both. Use [subtitle] only when it adds context the title cannot carry, such as the
 * name of the employee or office being shown.
 *
 * @param title title of the screen, shown on one line.
 * @param modifier modifier applied to the bar.
 * @param subtitle optional secondary line shown below the title.
 * @param onBack when not null, shows a back arrow that calls it.
 * @param onClose when not null and [onBack] is null, shows a close icon that calls it.
 * @param actions trailing actions of the bar, such as icon buttons or a menu.
 */
@Composable
fun FlowboardTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .heightIn(min = 72.dp)
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            when {
                onBack != null -> TopBarNavigationButton(
                    icon = FlowboardIcons.ArrowBack,
                    description = "Volver",
                    onClick = onBack
                )
                onClose != null -> TopBarNavigationButton(
                    icon = FlowboardIcons.Close,
                    description = "Cerrar",
                    onClick = onClose
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            actions()
        }
    }
}

/**
 * Standard navigation icon button at the start of a [FlowboardTopBar].
 *
 * It keeps the 48 dp touch target and is shifted to the left by [NAVIGATION_ICON_SHIFT], so the
 * arrow lines up with the screen margin instead of the button edge.
 *
 * @param icon icon drawn inside the button.
 * @param description accessibility description of the button.
 * @param onClick called when the button is tapped.
 */
@Composable
private fun TopBarNavigationButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.layout { measurable, constraints ->
            val shift = NAVIGATION_ICON_SHIFT.roundToPx()
            val placeable = measurable.measure(constraints)
            layout(placeable.width - shift, placeable.height) { placeable.place(-shift, 0) }
        }
    ) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
    }
}

/** Space between the edge of an icon button and its icon, removed so the icon aligns with the margin. */
private val NAVIGATION_ICON_SHIFT = 12.dp
