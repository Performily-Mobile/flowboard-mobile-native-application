package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    modifier: Modifier = Modifier,
    showEmployee: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick?.invoke() } else Modifier)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (showEmployee) {
                    Text(
                        record.employeeName ?: "Colaborador #${record.employeeId}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    record.workDate.format(DateTimeFormatter.ofPattern("EEE dd/MM")),
                    style = if (showEmployee) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                )
                Text(
                    when {
                        record.checkInTime != null && record.checkOutTime != null ->
                            "${record.checkInTime} – ${record.checkOutTime} · ${formatHours(record.effectiveHours)}${formatOvertime(record.overtimeHours)}"
                        record.checkInTime != null -> "${record.checkInTime} – sin salida"
                        else -> "Sin marcaciones"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AttendanceStatusChip(record.status)
        }
        HorizontalDivider(color = Divider)
    }
}

private fun formatHours(hours: Double?): String = hours?.let { String.format("%.1f h", it) } ?: "—"
private fun formatOvertime(hours: Double?): String = hours?.takeIf { it > 0 }?.let { " · ${String.format("%.1f h extra", it)}" } ?: ""
