package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.presentation.state.AreaForm

/**
 * Bottom sheet to create a new area.
 *
 * @param form current values and errors of the form.
 * @param isSaving true while the area is being saved.
 * @param onNameChange called when the name changes.
 * @param onDescriptionChange called when the description changes.
 * @param onSave called when the user confirms.
 * @param onDismiss called when the sheet is closed or cancelled.
 */
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            SheetHeader(title = "Nueva área")
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

/**
 * Title of a bottom sheet of the workspace feature, with an optional subtitle.
 *
 * @param title title of the sheet.
 * @param subtitle optional secondary line, such as the employee affected.
 */
@Composable
fun SheetHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.padding(bottom = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cancel and confirm buttons at the end of a bottom sheet, sharing the width.
 *
 * @param confirmLabel text of the confirm button.
 * @param enabled false to disable the confirm button.
 * @param onConfirm called when the confirm button is tapped.
 * @param onCancel called when the cancel button is tapped.
 */
@Composable
fun SheetActions(
    confirmLabel: String,
    enabled: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) { Text("Cancelar") }
        Button(
            onClick = onConfirm,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) { Text(confirmLabel) }
    }
}
