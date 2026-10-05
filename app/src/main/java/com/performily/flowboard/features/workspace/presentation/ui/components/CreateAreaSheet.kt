package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.presentation.state.AreaForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAreaSheet(
    form: AreaForm,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nueva área", style = MaterialTheme.typography.titleLarge)
            FormTextField(
                label = "Nombre del área",
                value = form.name,
                onValueChange = onNameChange,
                error = form.nameError
            )
            FormTextField(
                label = "Descripción (opcional)",
                value = form.description,
                onValueChange = onDescriptionChange,
                singleLine = false,
                minLines = 3
            )
            SheetActions(
                confirmLabel = "Crear área",
                enabled = !isSaving && form.name.isNotBlank(),
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}

@Composable
fun SheetActions(
    confirmLabel: String,
    enabled: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)
    ) {
        TextButton(onClick = onCancel) { Text("Cancelar") }
        Button(onClick = onConfirm, enabled = enabled, shape = RoundedCornerShape(8.dp)) { Text(confirmLabel) }
    }
}
