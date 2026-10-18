package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.presentation.state.ReinstateForm
import java.time.LocalDate

/**
 * Bottom sheet to bring a terminated employee back with a new position.
 *
 * @param employeeName name of the employee to reinstate.
 * @param form current values and errors of the form.
 * @param areas areas to choose from.
 * @param positions positions of the selected area.
 * @param isSaving true while the reinstatement is being saved.
 * @param errorMessage error returned by the last save, if any.
 * @param onAreaChange called with the id of the selected area.
 * @param onPositionChange called with the id of the selected position.
 * @param onDateChange called with the reinstatement date.
 * @param onSave called when the user confirms.
 * @param onDismiss called when the sheet is closed or cancelled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReinstateEmployeeSheet(
    employeeName: String,
    form: ReinstateForm,
    areas: List<Area>,
    positions: List<Position>,
    isSaving: Boolean,
    errorMessage: String?,
    onAreaChange: (Long) -> Unit,
    onPositionChange: (Long) -> Unit,
    onDateChange: (LocalDate) -> Unit,
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
            SheetHeader(
                title = "Reincorporar colaborador",
                subtitle = "$employeeName volverá a estado Activo con el puesto que elijas."
            )
            AreaDropdown(
                areas = areas,
                selectedAreaId = form.areaId,
                onSelect = { onAreaChange(it.id) },
                isError = form.areaError != null,
                supportingText = form.areaError,
                modifier = Modifier.fillMaxWidth()
            )
            PositionDropdown(
                positions = positions,
                selectedPositionId = form.positionId,
                onSelect = { onPositionChange(it.id) },
                enabled = form.areaId != null,
                isError = form.positionError != null,
                supportingText = form.positionError,
                modifier = Modifier.fillMaxWidth()
            )
            DatePickerField(
                label = "Fecha de reingreso",
                value = form.reinstatementDate,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            SheetActions(
                confirmLabel = "Reincorporar",
                enabled = !isSaving,
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}
