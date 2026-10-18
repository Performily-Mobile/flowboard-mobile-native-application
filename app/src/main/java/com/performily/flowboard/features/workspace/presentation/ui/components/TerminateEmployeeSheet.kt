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
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.presentation.state.TerminationForm
import java.time.LocalDate

/** Termination reasons offered by the form; the backend receives the text itself. */
val TerminationReasons = listOf(
    "Renuncia voluntaria",
    "Término de contrato",
    "Despido",
    "Mutuo acuerdo",
    "Jubilación"
)

/**
 * Dialog to register the termination of an employee.
 *
 * @param employeeName name of the employee.
 * @param form current values and errors of the form.
 * @param isSaving true while the termination is being saved.
 * @param errorMessage error returned by the last save, if any.
 * @param onReasonChange called with the selected reason.
 * @param onDateChange called with the termination date.
 * @param onConfirm called when the user confirms.
 * @param onDismiss called when the dialog is closed or cancelled.
 */
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
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
