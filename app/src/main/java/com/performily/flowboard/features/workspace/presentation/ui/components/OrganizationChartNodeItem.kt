package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.designsystem.theme.Outline
import com.performily.flowboard.core.designsystem.theme.PendingContainer
import com.performily.flowboard.core.designsystem.theme.PendingOutline
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChartNode

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
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            borderColor = if (isHighlighted) MaterialTheme.colorScheme.primary else Divider
        )
        if (node.subordinates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Outline,
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

@Composable
private fun NodeCard(
    initials: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Divider
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeeAvatar(initials = initials, size = 32.dp)
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
