package com.performily.flowboard.features.benefits.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.presentation.state.AdjustmentForm
import com.performily.flowboard.features.benefits.presentation.state.BenefitTypeForm
import com.performily.flowboard.features.benefits.presentation.state.DeliveryForm
import java.time.LocalDate

/**
 * New benefit type sheet (MA-60).
 *
 * Collects the name, the unit of measure and whether the type tracks a balance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBenefitTypeSheet(
    form: BenefitTypeForm,
    onNameChange: (String) -> Unit,
    onUnitChange: (BenefitUnit) -> Unit,
    onHasBalanceChange: (Boolean) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!form.isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nuevo tipo de beneficio", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                isError = form.nameError != null,
                supportingText = form.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            BenefitsSelectField(
                label = "Unidad de medida",
                options = BenefitUnit.entries,
                selected = form.unit,
                optionLabel = { it.label },
                onSelect = onUnitChange,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Maneja saldo", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = form.hasBalance, onCheckedChange = onHasBalanceChange)
            }
            DialogActions(
                confirmLabel = "Crear",
                enabled = !form.isSaving && form.name.isNotBlank(),
                isLoading = form.isSaving,
                onConfirm = onCreate,
                onCancel = onDismiss
            )
        }
    }
}

/**
 * Benefit type status confirmation (MA-60).
 *
 * Asks the user to confirm activating or deactivating a catalog type.
 */
@Composable
fun ToggleBenefitTypeDialog(
    benefitType: BenefitType,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val deactivate = benefitType.active
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (deactivate) "¿Desactivar ${benefitType.name}?" else "¿Activar ${benefitType.name}?") },
        text = {
            Text(
                if (deactivate) {
                    "Seguirá en el catálogo y en las asignaciones existentes, pero ya no se podrá asignar."
                } else {
                    "Volverá a estar disponible para asignar."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) { Text(if (deactivate) "Desactivar" else "Activar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

/**
 * Register delivery dialog (MA-62).
 *
 * A delivery can only be registered once.
 */
@Composable
fun RegisterDeliveryDialog(
    form: DeliveryForm,
    onDateChange: (LocalDate) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val assignment = form.assignment
    AlertDialog(
        onDismissRequest = { if (!form.isSaving) onDismiss() },
        title = { Text("Registrar entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${assignment.benefitTypeName} · ${BenefitsFormatters.quantity(assignment.quantity, assignment.unit)} " +
                        "para ${assignment.employeeName}. Una vez registrada no podrá registrarse otra vez.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BenefitsDateField(
                    label = "Fecha de entrega",
                    value = form.deliveredOn,
                    onValueChange = onDateChange,
                    minDate = assignment.startDate,
                    maxDate = LocalDate.now(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                form.error?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !form.isSaving) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !form.isSaving) { Text("Cancelar") }
        }
    )
}

/**
 * Adjust balance sheet (MA-63).
 *
 * Adds or deducts vacation days with a mandatory reason.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceSheet(
    form: AdjustmentForm,
    onOperationChange: (AdjustmentOperation) -> Unit,
    onDaysChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!form.isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ajustar saldo", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "El ajuste quedará en el historial con tu nombre y el motivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BenefitsSegmented(
                options = AdjustmentOperation.entries,
                selected = form.operation,
                optionLabel = { it.label },
                onSelect = onOperationChange
            )
            OutlinedTextField(
                value = form.days,
                onValueChange = onDaysChange,
                label = { Text("Cantidad de días") },
                isError = form.daysError != null,
                supportingText = form.daysError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.reason,
                onValueChange = onReasonChange,
                label = { Text("Motivo del ajuste") },
                isError = form.reasonError != null,
                supportingText = form.reasonError?.let { { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            form.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            DialogActions(
                confirmLabel = "Guardar ajuste",
                enabled = !form.isSaving,
                isLoading = form.isSaving,
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}

@Composable
private fun DialogActions(
    confirmLabel: String,
    enabled: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onClick = onCancel, enabled = !isLoading) { Text("Cancelar") }
        Button(onClick = onConfirm, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(confirmLabel)
            }
        }
    }
}
