package com.performily.flowboard.features.workspace.presentation.ui.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment

/**
 * "Puesto" tab of the employee record: current job data and the history of job assignments.
 *
 * @param employee employee shown.
 * @param directManagerName name of the direct manager, when it is known.
 * @param jobAssignments history of job assignments, newest first.
 * @param modifier modifier applied to the tab.
 */
@Composable
fun JobAssignmentTab(
    employee: Employee,
    directManagerName: String?,
    jobAssignments: List<JobAssignment>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)) {
        InfoPanel {
            DetailRow(label = "Área", value = employee.areaName)
            DetailRow(label = "Posición", value = employee.positionTitle)
            DetailRow(
                label = "Jefe directo",
                value = directManagerName ?: if (employee.hasDirectManager) "—" else "Sin jefe directo"
            )
            DetailRow(label = "Tipo de contrato", value = employee.contractType.label())
            DetailRow(label = "Fecha de ingreso", value = employee.employmentPeriod.hireDate.toDisplay())
            employee.employmentPeriod.contractEndDate?.let {
                DetailRow(label = "Fin de contrato", value = it.toDisplay())
            }
        }
        employee.termination?.let { termination ->
            InfoPanel {
                DetailRow(label = "Fecha de cese", value = termination.terminationDate.toDisplay())
                DetailRow(label = "Motivo", value = termination.reason)
            }
        }

        if (jobAssignments.isNotEmpty()) {
            SectionTitle("Historial de puestos")
            jobAssignments.forEach { assignment -> JobAssignmentItem(assignment) }
        }
    }
}

/** Card with one entry of the job assignment history. */
@Composable
private fun JobAssignmentItem(assignment: JobAssignment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${assignment.positionTitle} · ${assignment.areaName}",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val period = assignment.startDate.toDisplay() + " – " +
                    (assignment.endDate?.toDisplay() ?: "Actual")
                Text(
                    text = "${assignment.changeType.label()} · $period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (assignment.current) {
                LabelChip(text = "Vigente", containerColor = ActiveContainer)
            }
        }
    }
}
