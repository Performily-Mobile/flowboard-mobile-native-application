package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.AwaitingContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus

/**
 * Pill with the payment status of a payslip: Pendiente, Pagado or Observado (MA-65, MA-69).
 *
 * @param status payment status to show.
 * @param modifier modifier applied to the pill.
 */
@Composable
fun PaymentStatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val container = when (status) {
        PaymentStatus.PENDING -> AwaitingContainer
        PaymentStatus.PAID -> ActiveContainer
        PaymentStatus.OBSERVED -> ErrorContainer
    }
    PayrollChip(text = status.label(), containerColor = container, modifier = modifier)
}

/**
 * Pill with the publication status of a payslip: "Por publicar" or "Publicada" (MA-67).
 *
 * @param status publication status to show.
 * @param modifier modifier applied to the pill.
 */
@Composable
fun PublicationStatusChip(status: PublicationStatus, modifier: Modifier = Modifier) {
    val container = when (status) {
        PublicationStatus.UNDER_REVIEW -> AwaitingContainer
        PublicationStatus.PUBLISHED -> ActiveContainer
    }
    PayrollChip(text = status.label(), containerColor = container, modifier = modifier)
}

/** Pill shared by the payroll status chips. */
@Composable
private fun PayrollChip(text: String, containerColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface, maxLines = 1)
    }
}
