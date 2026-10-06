package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.presentation.state.TerminationForm
import java.time.LocalDate

/** Motivos de cese que ofrece el formulario (el backend recibe el texto). */
val TerminationReasons = listOf(
    "Renuncia voluntaria",
    "Término de contrato",
    "Despido",
    "Mutuo acuerdo",
    "Jubilación"
)

@Composable
fun TerminateEmployeeSheet(
    employeeName: String,
    form: TerminationForm,
    isSaving: Boolean,
    errorMessage: String?,
    onReasonChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar cese") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$employeeName pasará a estado Cesado y su acceso a Flowboard se inhabilitará de inmediato.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DatePickerField(
                    label = "Fecha de cese",
                    value = form.terminationDate,
                    onValueChange = onDateChange,
                    isError = form.dateError != null,
                    supportingText = form.dateError,
                    modifier = Modifier.fillMaxWidth()
                )
                SelectField(
                    label = "Motivo",
                    options = TerminationReasons,
                    selected = form.reason,
                    optionLabel = { it },
                    onSelect = onReasonChange,
                    isError = form.reasonError != null,
                    supportingText = form.reasonError,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("Registrar cese") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
