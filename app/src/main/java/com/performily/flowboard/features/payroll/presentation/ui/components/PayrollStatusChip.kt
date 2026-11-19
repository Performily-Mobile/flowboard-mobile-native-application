package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardStatusBadge
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus

/**
 * Badge with the payment status of a payslip: Pendiente, Pagado or Observado (MA-65, MA-69).
 *
 * @param status payment status to show.
 * @param modifier modifier applied to the badge.
 */
@Composable
fun PaymentStatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val tone = when (status) {
        PaymentStatus.PENDING -> StatusTone.WARNING
        PaymentStatus.PAID -> StatusTone.SUCCESS
        PaymentStatus.OBSERVED -> StatusTone.DANGER
    }
    FlowboardStatusBadge(text = status.label(), tone = tone, modifier = modifier)
}

/**
 * Badge with the publication status of a payslip: "Por publicar" or "Publicada" (MA-67).
 *
 * @param status publication status to show.
 * @param modifier modifier applied to the badge.
 */
@Composable
fun PublicationStatusChip(status: PublicationStatus, modifier: Modifier = Modifier) {
    val tone = when (status) {
        PublicationStatus.UNDER_REVIEW -> StatusTone.WARNING
        PublicationStatus.PUBLISHED -> StatusTone.SUCCESS
    }
    FlowboardStatusBadge(text = status.label(), tone = tone, modifier = modifier)
}
