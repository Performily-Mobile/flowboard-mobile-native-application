package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardStatusBadge
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus

/**
 * Badge with the employment status of an employee.
 *
 * @param status status to show.
 * @param modifier modifier applied to the badge.
 */
@Composable
fun StatusChip(
    status: EmploymentStatus,
    modifier: Modifier = Modifier
) {
    val tone = when (status) {
        EmploymentStatus.ACTIVE -> StatusTone.SUCCESS
        EmploymentStatus.SUSPENDED -> StatusTone.WARNING
        EmploymentStatus.TERMINATED -> StatusTone.DANGER
    }
    FlowboardStatusBadge(text = status.label(), tone = tone, modifier = modifier)
}

/**
 * Badge that tells whether an area or a position is active.
 *
 * @param active true when the item is active.
 * @param modifier modifier applied to the badge.
 * @param activeLabel text shown when the item is active.
 * @param inactiveLabel text shown when the item is inactive.
 */
@Composable
fun ActiveChip(
    active: Boolean,
    modifier: Modifier = Modifier,
    activeLabel: String = "Activo",
    inactiveLabel: String = "Inactivo"
) {
    FlowboardStatusBadge(
        text = if (active) activeLabel else inactiveLabel,
        tone = if (active) StatusTone.SUCCESS else StatusTone.NEUTRAL,
        modifier = modifier
    )
}
