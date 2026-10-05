package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.presentation.state.RegisterEmployeeUiState
import com.performily.flowboard.features.workspace.presentation.state.RegisterField
import java.time.LocalDate

@Composable
fun ContractAssignmentStep(
    state: RegisterEmployeeUiState,
    onContractTypeChange: (ContractType) -> Unit,
    onHireDateChange: (LocalDate) -> Unit,
    onContractEndDateChange: (LocalDate) -> Unit,
    onAreaChange: (Long) -> Unit,
    onPositionChange: (Long) -> Unit,
    onDirectManagerChange: (EmployeeId?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectField(
            label = "Tipo de contrato",
            options = ContractType.entries,
            selected = state.contractType,
            optionLabel = { it.label() },
            onSelect = onContractTypeChange,
            modifier = Modifier.fillMaxWidth()
        )
        DatePickerField(
            label = "Fecha de ingreso",
            value = state.hireDate,
            onValueChange = onHireDateChange,
            isError = state.errorOf(RegisterField.HIRE_DATE) != null,
            supportingText = state.errorOf(RegisterField.HIRE_DATE),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.contractType == ContractType.FIXED_TERM) {
            DatePickerField(
                label = "Fecha de fin de contrato",
                value = state.contractEndDate,
                onValueChange = onContractEndDateChange,
                isError = state.errorOf(RegisterField.CONTRACT_END_DATE) != null,
                supportingText = state.errorOf(RegisterField.CONTRACT_END_DATE),
                modifier = Modifier.fillMaxWidth()
            )
        }
        AreaDropdown(
            areas = state.areas,
            selectedAreaId = state.areaId,
            onSelect = { onAreaChange(it.id) },
            isError = state.errorOf(RegisterField.AREA) != null,
            supportingText = state.errorOf(RegisterField.AREA),
            modifier = Modifier.fillMaxWidth()
        )
        PositionDropdown(
            positions = state.positions,
            selectedPositionId = state.positionId,
            onSelect = { onPositionChange(it.id) },
            enabled = state.areaId != null,
            isError = state.errorOf(RegisterField.POSITION) != null,
            supportingText = state.errorOf(RegisterField.POSITION)
                ?: state.selectedPosition?.let { "Sueldo mínimo referencial del puesto: ${it.referenceSalary.toDisplay()}" },
            modifier = Modifier.fillMaxWidth()
        )
        EmployeeDropdown(
            label = "Jefe directo",
            employees = state.managers,
            selectedEmployeeId = state.directManagerId,
            onSelect = { onDirectManagerChange(it?.id) },
            modifier = Modifier.fillMaxWidth()
        )
        InfoBanner(text = "Al registrar, RR.HH. generará sus credenciales de acceso y le entregará la contraseña temporal.")
    }
}

@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = FlowboardIcons.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
