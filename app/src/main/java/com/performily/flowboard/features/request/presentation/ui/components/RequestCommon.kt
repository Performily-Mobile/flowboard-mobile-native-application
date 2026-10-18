package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.theme.OnAvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus


/** Pill with the label of a request status, tinted by the status. */
@Composable
fun RequestStatusChip(status: RequestStatus, modifier: Modifier = Modifier) {
    RequestChip(text = status.label, containerColor = RequestColors.status(status), modifier = modifier)
}


/** Small pill-shaped label with a tinted background. */
@Composable
fun RequestChip(text: String, containerColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface, maxLines = 1)
    }
}


/** Circle with the initials of a person. */
@Composable
fun RequestAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = RequestFormatters.initials(name),
            fontSize = if (size > 40.dp) 17.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnAvatarContainer
        )
    }
}


/** Icon drawn on a tinted circle. */
@Composable
fun RequestIconCircle(
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


/**
 * Card used by every list and panel of the request feature.
 *
 * @param modifier modifier applied to the card.
 * @param onClick called when the card is tapped, or null for a static card.
 * @param containerColor background color of the card.
 * @param border border of the card, or null for none.
 * @param spacing vertical gap between the children.
 * @param content children laid out in a column.
 */
@Composable
fun RequestCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.White,
    border: BorderStroke? = BorderStroke(1.dp, Divider),
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content
        )
    }
}


/** Label and value shown as one row of a [KeyValueCard]. */
data class KeyValue(val key: String, val value: String)

/** Card that lists label and value pairs separated by dividers; shows nothing when [items] is empty. */
@Composable
fun KeyValueCard(items: List<KeyValue>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    RequestCard(modifier = modifier, spacing = 0.dp) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.key,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = item.value, style = MaterialTheme.typography.bodyLarge)
            }
            if (index < items.lastIndex) HorizontalDivider(color = Divider)
        }
    }
}

/** Tinted banner with an icon, a message and an optional text action. */
@Composable
fun RequestBanner(
    message: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, MaterialTheme.shapes.medium)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = contentColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


/** Step label and segmented progress bar of a multi-step form. */
@Composable
fun StepHeader(step: Int, total: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Paso $step de $total · $label",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}


/** Title of a block with an optional action on the right. */
@Composable
fun RequestSectionHeader(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        action?.invoke()
    }
}

/** Centered progress indicator that fills the available space. */
@Composable
fun RequestLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Empty or error state with an optional icon and action button. */
@Composable
fun RequestMessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.xxl, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
            }
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
