package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.PrimaryContainer
import com.performily.flowboard.core.designsystem.theme.SuspendedContainer
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.domain.valueobject.label

/**
 * Pill that shows the status of an attendance record.
 *
 * @param status status to show.
 * @param modifier modifier applied to the pill.
 */
@Composable
fun AttendanceStatusChip(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val background = when (status) {
        AttendanceStatus.ON_TIME -> ActiveContainer
        AttendanceStatus.LATE -> SuspendedContainer
        AttendanceStatus.ABSENT -> ErrorContainer
        AttendanceStatus.JUSTIFIED -> PrimaryContainer
        AttendanceStatus.INCOMPLETE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Text(
        text = status.label(),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        modifier = modifier
            .background(background, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}
