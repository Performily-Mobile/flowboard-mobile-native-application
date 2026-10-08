package com.performily.flowboard.features.benefits.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.performily.flowboard.features.benefits.domain.entity.VacationMovement
import com.performily.flowboard.features.benefits.domain.valueobject.VacationMovementType

/**
 * Un movimiento del saldo de vacaciones.
 * En la vista del colaborador (MA-57) los días van a la derecha; en la de RR.HH.
 * (MA-63) van en el detalle: "−5 días · 10/08/2026".
 */
@Composable
fun VacationMovementRow(
    movement: VacationMovement,
    modifier: Modifier = Modifier,
    daysInSubtitle: Boolean = false
) {
    val days = BenefitsFormatters.signedDays(movement.days)
    val detail = BenefitsFormatters.movementDetail(movement)
    BenefitListRow(
        title = BenefitsFormatters.movementTitle(movement),
        subtitle = if (daysInSubtitle) "$days · $detail" else detail,
        modifier = modifier,
        leading = { IconCircle(icon = iconFor(movement)) },
        trailing = {
            if (!daysInSubtitle) {
                Text(
                    text = days,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

private fun iconFor(movement: VacationMovement): ImageVector = when (movement.type) {
    VacationMovementType.MANUAL_ADJUSTMENT -> BenefitsIcons.Edit
    VacationMovementType.REVERSAL -> BenefitsIcons.Sync
    else -> if (movement.days.signum() < 0) BenefitsIcons.Minus else BenefitsIcons.Plus
}
