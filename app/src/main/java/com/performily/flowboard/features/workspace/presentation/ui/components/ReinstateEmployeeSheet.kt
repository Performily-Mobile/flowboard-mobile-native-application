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
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.presentation.state.ReinstateForm
import java.time.LocalDate

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reincorporar colaborador", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "$employeeName volverá a estado Activo con el puesto que elijas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
