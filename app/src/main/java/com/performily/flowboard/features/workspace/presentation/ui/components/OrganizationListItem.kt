package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.performily.flowboard.core.designsystem.component.FlowboardLeadingIcon
import com.performily.flowboard.core.designsystem.component.FlowboardListRow

/**
 * Row used by the lists of areas and positions.
 *
 * @param icon icon shown in the leading circle.
 * @param title main text.
 * @param subtitle secondary text.
 * @param modifier modifier applied to the row.
 * @param trailing optional content at the end of the row (amount, badge, menu).
 */
@Composable
fun OrganizationListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    FlowboardListRow(
        headline = title,
        supporting = subtitle,
        modifier = modifier,
        leading = { FlowboardLeadingIcon(icon = icon) },
        trailing = trailing
    )
}
