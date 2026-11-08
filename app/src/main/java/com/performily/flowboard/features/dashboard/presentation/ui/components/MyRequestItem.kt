package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.AwaitingContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.theme.SuspendedContainer
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
        Surface(shape = CircleShape, color = statusColor(request.status)) {
            Text(
                text = request.status.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

/** Returns the background of the status chip of a request. */
@Composable
private fun statusColor(status: MyRequestStatus): Color = when (status) {
    MyRequestStatus.PENDING -> AwaitingContainer
    MyRequestStatus.UNDER_REVIEW -> SuspendedContainer
    MyRequestStatus.APPROVED -> ActiveContainer
    MyRequestStatus.REJECTED -> ErrorContainer
    MyRequestStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceContainerHigh
}
