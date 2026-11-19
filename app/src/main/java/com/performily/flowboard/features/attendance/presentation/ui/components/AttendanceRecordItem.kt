package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardListRow
import com.performily.flowboard.core.text.AppLocale
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import java.time.format.DateTimeFormatter

/**
 * Row with the date, punches and status of one attendance record.
 *
 * In HR lists the row starts with the employee avatar and name, like every other list of people;
 * in the employee's own history the date becomes the first line.
 *
 * @param record record to show.
 * @param modifier modifier applied to the row.
 * @param showEmployee true to show the employee avatar and name (HR lists).
 * @param onClick called when the row is tapped, or null when the row is not tappable.
 */
@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    modifier: Modifier = Modifier,
    showEmployee: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val date = record.workDate.format(DayFormatter).replace(".", "").replaceFirstChar { it.uppercase(AppLocale) }
    val punches = when {
        record.checkInTime != null && record.checkOutTime != null ->
            "${record.checkInTime} – ${record.checkOutTime} · ${formatHours(record.effectiveHours)}${formatOvertime(record.overtimeHours)}"
        record.checkInTime != null -> "${record.checkInTime} – sin salida"
        else -> "Sin marcaciones"
    }
    val employeeName = record.employeeName ?: "Colaborador #${record.employeeId}"
    FlowboardListRow(
        headline = if (showEmployee) employeeName else date,
        supporting = if (showEmployee) date else punches,
        meta = if (showEmployee) punches else null,
        modifier = modifier,
        onClick = onClick,
        leading = if (showEmployee) {
            { AttendanceEmployeeAvatar(name = employeeName) }
        } else {
            null
        },
        trailing = { AttendanceStatusChip(record.status) }
    )
}

/** Short weekday, day and month in Spanish, for example "Mié 18 nov". */
private val DayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", AppLocale)

private fun formatHours(hours: Double?): String = hours?.let { String.format(AppLocale, "%.1f h", it) } ?: "—"
private fun formatOvertime(hours: Double?): String =
    hours?.takeIf { it > 0 }?.let { " · ${String.format(AppLocale, "%.1f h extra", it)}" } ?: ""
