package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.presentation.state.PositionForm

/**
 * Bottom sheet to create a new position inside an active area.
 *
 * @param form current values and errors of the form.
 * @param areas active areas the position can belong to.
 * @param isSaving true while the position is being saved.
 * @param onAreaChange called with the id of the selected area.
 * @param onTitleChange called when the title changes.
 * @param onSalaryChange called when the reference salary changes.
 * @param onSave called when the user confirms.
 * @param onDismiss called when the sheet is closed or cancelled.
 */
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            SheetHeader(title = "Nueva posición")
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
