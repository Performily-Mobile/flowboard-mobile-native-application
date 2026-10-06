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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.presentation.state.ReassignJobForm
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReassignJobSheet(
    employeeName: String,
    form: ReassignJobForm,
    areas: List<Area>,
    positions: List<Position>,
    managerCandidates: List<Employee>,
    isSaving: Boolean,
    errorMessage: String?,
    onAreaChange: (Long) -> Unit,
    onPositionChange: (Long) -> Unit,
    onManagerChange: (EmployeeId?) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reasignar puesto", style = MaterialTheme.typography.titleLarge)
            Text(
                text = employeeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AreaDropdown(
                areas = areas,
                selectedAreaId = form.areaId,
                onSelect = { onAreaChange(it.id) },
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
            EmployeeDropdown(
                label = "Jefe directo",
                employees = managerCandidates,
                selectedEmployeeId = form.directManagerId,
                onSelect = { onManagerChange(it?.id) },
                isError = form.directManagerError != null,
                supportingText = form.directManagerError,
                modifier = Modifier.fillMaxWidth()
            )
            DatePickerField(
                label = "Fecha efectiva",
                value = form.effectiveDate,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth()
            )
            InfoBanner(text = "El cambio de puesto quedará registrado en el historial del colaborador.")
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            SheetActions(
                confirmLabel = "Guardar cambios",
                enabled = !isSaving,
                onConfirm = onSave,
                onCancel = onDismiss
            )
        }
    }
}
