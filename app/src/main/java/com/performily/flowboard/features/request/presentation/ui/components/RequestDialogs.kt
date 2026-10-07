package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType

@Composable
fun CancelRequestDialog(
    message: String,
    isCancelling: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        containerColor = RequestColors.Dialog,
        title = { Text("¿Cancelar la solicitud?") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isCancelling) {
                if (isCancelling) LoadingDot() else Text("Cancelar solicitud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCancelling) { Text("Volver") }
        }
    )
}


@Composable
fun RejectRequestDialog(
    requesterName: String,
    reason: String,
    error: String?,
    isSaving: Boolean,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = RequestColors.Dialog,
        title = { Text("Rechazar solicitud") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "$requesterName verá el motivo en el detalle de su solicitud y recibirá una notificación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { onReasonChange(it.take(MAX_COMMENT)) },
                    label = { Text("Motivo del rechazo") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                if (isSaving) LoadingDot() else Text("Rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnForReviewSheet(
    requesterFirstName: String,
    comment: String,
    error: String?,
    isSaving: Boolean,
    onCommentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RequestColors.Sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Devolver a revisión", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "La solicitud pasará a \"En revisión\" y volverá a tu bandeja cuando $requesterFirstName la reenvíe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = comment,
                onValueChange = { onCommentChange(it.take(MAX_COMMENT)) },
                label = { Text("Comentario para el colaborador") },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            SheetActions(
                confirmLabel = "Devolver",
                isLoading = isSaving,
                onConfirm = onConfirm,
                onCancel = onDismiss
            )
        }
    }
}

@Composable
fun DeleteRequestTypeDialog(
    typeName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        containerColor = RequestColors.Dialog,
        title = { Text("¿Eliminar $typeName?") },
        text = { Text("Solo se puede eliminar si nadie lo ha usado. Si ya tiene solicitudes, podrás desactivarlo.") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) LoadingDot() else Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldSheet(
    label: String,
    dataType: FieldDataType,
    required: Boolean,
    error: String?,
    onLabelChange: (String) -> Unit,
    onDataTypeChange: (FieldDataType) -> Unit,
    onRequiredChange: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RequestColors.Sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Agregar campo", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = label,
                onValueChange = { onLabelChange(it.take(MAX_LABEL)) },
                label = { Text("Nombre del campo") },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            RequestSelectField(
                label = "Tipo de dato",
                options = FieldDataType.entries,
                selected = dataType,
                optionLabel = { it.label },
                onSelect = onDataTypeChange,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Obligatorio", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = required, onCheckedChange = onRequiredChange)
            }
            SheetActions(confirmLabel = "Agregar", isLoading = false, onConfirm = onAdd, onCancel = onDismiss)
        }
    }
}

@Composable
private fun SheetActions(
    confirmLabel: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onClick = onCancel, enabled = !isLoading) { Text("Cancelar") }
        Button(onClick = onConfirm, enabled = !isLoading, shape = RoundedCornerShape(8.dp)) {
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

@Composable
private fun LoadingDot() {
    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
}

private const val MAX_COMMENT = 500
private const val MAX_LABEL = 80
