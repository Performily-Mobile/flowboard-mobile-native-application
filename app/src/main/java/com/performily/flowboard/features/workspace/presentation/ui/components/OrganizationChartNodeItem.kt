package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.designsystem.theme.OutlineVariant
import com.performily.flowboard.core.designsystem.theme.PendingContainer
import com.performily.flowboard.core.designsystem.theme.PendingOutline
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChartNode

/**
 * Node of the organization chart: the employee card followed by its direct reports, indented
 * behind a vertical guide line.
 *
 * @param node node to draw, with its subordinates.
 * @param modifier modifier applied to the node.
 * @param highlightedEmployeeId employee to highlight as the current user.
 */
@Composable
fun OrganizationChartNodeItem(
    node: OrganizationChartNode,
    modifier: Modifier = Modifier,
    highlightedEmployeeId: EmployeeId? = null
) {
    val isHighlighted = node.employeeId == highlightedEmployeeId
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NodeCard(
            initials = node.initials,
            title = if (isHighlighted) "${node.fullName} (tú)" else node.fullName,
            subtitle = node.positionTitle,
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            borderColor = if (isHighlighted) MaterialTheme.colorScheme.primary else null
        )
        if (node.subordinates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = OutlineVariant,
                            start = Offset(1.dp.toPx(), 0f),
                            end = Offset(1.dp.toPx(), size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                node.subordinates.forEach { child -> OrganizationChartNodeItem(child, highlightedEmployeeId = highlightedEmployeeId) }
            }
        }
    }
}

/**
 * Card of an employee whose direct manager was terminated and who waits for a new one.
 *
 * @param node node of the employee.
 * @param modifier modifier applied to the card.
 */
@Composable
fun PendingNodeCard(node: OrganizationChartNode, modifier: Modifier = Modifier) {
    NodeCard(
        initials = node.initials,
        title = node.fullName,
        subtitle = "${node.positionTitle} · jefe cesado",
        containerColor = PendingContainer,
        borderColor = PendingOutline,
        modifier = modifier
    )
}

/** Tonal card with avatar, name and position of one employee of the chart. */
@Composable
private fun NodeCard(
    initials: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeeAvatar(initials = initials, size = 32.dp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
