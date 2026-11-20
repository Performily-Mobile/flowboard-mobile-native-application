package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.component.FlowboardStatusBadge
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.features.dashboard.domain.entity.MyRequest
import com.performily.flowboard.features.dashboard.domain.entity.MyRequestStatus

/**
 * Request of the employee: type and dates on the left and the status chip on the right.
 *
 * @param request request to display.
 * @param onClick called when the row is tapped.
 * @param modifier modifier applied to the row.
 */
@Composable
internal fun MyRequestItem(
    request: MyRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = request.typeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = EmployeeDashboardFormatters.requestDetail(request),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        FlowboardStatusBadge(text = request.status.label, tone = statusTone(request.status))
    }
}

/** Meaning of each request status, so the badge follows the light and dark themes. */
private fun statusTone(status: MyRequestStatus): StatusTone = when (status) {
    MyRequestStatus.PENDING -> StatusTone.WARNING
    MyRequestStatus.UNDER_REVIEW -> StatusTone.INFO
    MyRequestStatus.APPROVED -> StatusTone.SUCCESS
    MyRequestStatus.REJECTED -> StatusTone.DANGER
    MyRequestStatus.CANCELLED -> StatusTone.NEUTRAL
}
