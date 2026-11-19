package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardStatusBadge
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.domain.valueobject.label

/**
 * Badge that shows the status of an attendance record.
 *
 * @param status status to show.
 * @param modifier modifier applied to the badge.
 */
@Composable
fun AttendanceStatusChip(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val tone = when (status) {
        AttendanceStatus.ON_TIME -> StatusTone.SUCCESS
        AttendanceStatus.LATE -> StatusTone.WARNING
        AttendanceStatus.ABSENT -> StatusTone.DANGER
        AttendanceStatus.JUSTIFIED -> StatusTone.INFO
        AttendanceStatus.INCOMPLETE -> StatusTone.NEUTRAL
    }
    FlowboardStatusBadge(text = status.label(), tone = tone, modifier = modifier)
}
