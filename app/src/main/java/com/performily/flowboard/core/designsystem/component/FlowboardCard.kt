package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Card used by every list and panel of the app.
 *
 * Cards are tonal: a soft surface container without border or shadow, the same look as the groups
 * of the "Más" screen, so lists, panels and their loading skeletons read as one family.
 *
 * @param modifier modifier applied to the card.
 * @param onClick called when the card is tapped, or null for a static card.
 * @param containerColor background of the card; use a container role, never a raw color.
 * @param contentPadding inner padding of the card.
 * @param verticalSpacing gap between the children.
 * @param content children laid out in a column.
 */
@Composable
fun FlowboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentPadding: PaddingValues = PaddingValues(Spacing.cardPadding),
    verticalSpacing: Dp = Spacing.s,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, color = containerColor) { body() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = containerColor) { body() }
    }
}

/**
 * Standard row of a list: optional leading avatar or icon, up to three lines of text and an optional
 * trailing element, all inside a [FlowboardCard].
 *
 * The first line uses bodyLarge in medium weight, the second bodyMedium and the third bodySmall, so
 * every list of the app keeps the same hierarchy and the same 72 dp minimum height.
 *
 * @param headline first line, usually a name or a title.
 * @param modifier modifier applied to the row.
 * @param supporting optional second line with the main detail.
 * @param meta optional third line with secondary data such as a date.
 * @param supportingMaxLines maximum lines of [supporting] before it is cut.
 * @param leading optional content at the start, usually an avatar.
 * @param trailing optional content at the end, such as a badge, an amount or a menu.
 * @param onClick called when the row is tapped, or null when the row is not tappable.
 */
@Composable
fun FlowboardListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    meta: String? = null,
    supportingMaxLines: Int = 1,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    FlowboardCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(start = Spacing.l, end = if (trailing != null) Spacing.m else Spacing.l, top = 14.dp, bottom = 14.dp),
        verticalSpacing = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.listRowMinHeight - 28.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!supporting.isNullOrBlank()) {
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = supportingMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!meta.isNullOrBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

/**
 * Icon on a tonal circle, the leading element of rows that do not represent a person.
 *
 * @param icon icon to draw.
 * @param modifier modifier applied to the circle.
 * @param containerColor background of the circle.
 * @param contentColor tint of the icon.
 */
@Composable
fun FlowboardLeadingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
    }
}
