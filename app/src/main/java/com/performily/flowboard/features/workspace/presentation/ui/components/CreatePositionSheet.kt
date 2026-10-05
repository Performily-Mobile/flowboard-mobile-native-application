package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.presentation.state.PositionForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePositionSheet(
    form: PositionForm,
    areas: List<Area>,
    isSaving: Boolean,
    onAreaChange: (Long) -> Unit,
    onTitleChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
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
            Text("Nueva posición", style = MaterialTheme.typography.titleLarge)
            AreaDropdown(
                areas = areas,
                selectedAreaId = form.areaId,
                onSelect = { onAreaChange(it.id) },
                isError = form.areaError != null,
                supportingText = form.areaError,
                modifier = Modifier.fillMaxWidth()
            )
            FormTextField(
                label = "Nombre de la posición",
                value = form.title,
                onValueChange = onTitleChange,
                error = form.titleError
            )
            FormTextField(
                label = "Sueldo mínimo referencial (S/)",
                value = form.referenceSalary,
                onValueChange = onSalaryChange,
                error = form.salaryError,
                keyboardType = KeyboardType.Decimal
            )
            SheetActions(
                confirmLabel = "Crear posición",
                enabled = !isSaving && form.title.isNotBlank() && form.referenceSalary.isNotBlank(),
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}
