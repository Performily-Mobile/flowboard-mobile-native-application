package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardListRow
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus

/**
 * Row of the employee list with avatar, name and job.
 *
 * Almost every employee is active, so the status badge is shown only for suspended or terminated
 * employees, where it tells something the list does not already say.
 *
 * @param employee employee shown.
 * @param onClick called when the row is tapped.
 * @param modifier modifier applied to the row.
 */
@Composable
fun EmployeeListItem(
    employee: Employee,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardListRow(
        headline = employee.name.sortableName,
        supporting = employee.jobDescription,
        supportingMaxLines = 2,
        modifier = modifier,
        onClick = onClick,
        leading = { EmployeeAvatar(initials = employee.name.initials) },
        trailing = if (employee.status != EmploymentStatus.ACTIVE) {
            { StatusChip(status = employee.status) }
        } else {
            null
        }
    )
}
