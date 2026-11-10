package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.AvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnAvatarContainer
import com.performily.flowboard.features.payroll.domain.entity.Payslip

/**
 * Tonal card of a payslip in the list of the employee: period, net amount, deposit summary and
 * the payment status chip.
 *
 * @param payslip payslip to display.
 * @param onClick called when the card is tapped.
 * @param modifier modifier applied to the card.
 */
@Composable
fun MyPayslipCard(
    payslip: Payslip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AvatarContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FlowboardIcons.Article,
                    contentDescription = null,
                    tint = OnAvatarContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = payslip.period.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = netAmountLabel(payslip.netAmount.toDisplay()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = MyPayslipFormatters.paymentSummary(payslip.payment),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PaymentStatusChip(status = payslip.payment.status)
        }
    }
}
