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

/** MA-68 · El colaborador ya tiene una boleta en el período: reemplazarla o cancelar. */
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
