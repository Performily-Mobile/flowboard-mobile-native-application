package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Employee

@Composable
fun TerminationBlockedDialog(
    employeeName: String,
    subordinates: List<Employee>,
    onSubordinateClick: (Employee) -> Unit,
    onReassignReports: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(FlowboardIcons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("No se puede registrar el cese") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val count = subordinates.size
                Text(
                    text = "$employeeName tiene $count ${if (count == 1) "colaborador" else "colaboradores"} a cargo. " +
                        "Asígnales otro jefe directo antes de registrar el cese.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                subordinates.forEach { subordinate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSubordinateClick(subordinate) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EmployeeAvatar(initials = subordinate.name.initials, size = 32.dp)
                        Column {
                            Text(text = subordinate.name.fullName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = subordinate.positionTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onReassignReports) { Text("Reasignar reportes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
