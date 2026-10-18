package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.OnErrorContainer

/**
 * Error banner of the payslip upload (MA-67), for example "boletas_setiembre.xlsx no es un formato permitido…".
 *
 * @param message error to show.
 * @param modifier modifier applied to the banner.
 */
@Composable
fun PayrollErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = FlowboardIcons.Warning,
            contentDescription = null,
            tint = OnErrorContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = OnErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}
