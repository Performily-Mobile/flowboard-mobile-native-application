package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** Chip de estado de pago: Pendiente, Pagado u Observado (MA-65, MA-69). */
@Composable
fun PaymentStatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val container = when (status) {
        PaymentStatus.PENDING -> AwaitingContainer
        PaymentStatus.PAID -> ActiveContainer
        PaymentStatus.OBSERVED -> ErrorContainer
    }
    PayrollChip(text = status.label(), containerColor = container, modifier = modifier)
}

/** Chip de publicación: "Por publicar" o "Publicada" (MA-67). */
@Composable
fun PublicationStatusChip(status: PublicationStatus, modifier: Modifier = Modifier) {
    val container = when (status) {
        PublicationStatus.UNDER_REVIEW -> AwaitingContainer
        PublicationStatus.PUBLISHED -> ActiveContainer
    }
    PayrollChip(text = status.label(), containerColor = container, modifier = modifier)
}

@Composable
private fun PayrollChip(text: String, containerColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface)
    }
}
