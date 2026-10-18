package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.DialogContainer

/**
 * Dialog shown when the employee already has a payslip in the period (MA-68): replace it or cancel.
 *
 * @param employeeName name of the employee.
 * @param periodLabel label of the payroll period.
 * @param onReplace called when the user replaces the payslip.
 * @param onCancel called when the user keeps the current payslip.
 */
@Composable
fun DuplicatePayslipDialog(
    employeeName: String,
    periodLabel: String,
    onReplace: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(FlowboardIcons.Warning, contentDescription = null) },
        title = { Text("Ya existe una boleta", textAlign = TextAlign.Center) },
        text = {
            Text(
                text = "$employeeName ya tiene una boleta cargada para $periodLabel. " +
                    "¿Deseas reemplazarla por el archivo nuevo?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = { TextButton(onClick = onReplace) { Text("Reemplazar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } },
        containerColor = DialogContainer,
        iconContentColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
