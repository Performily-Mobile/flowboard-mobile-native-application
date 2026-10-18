package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.presentation.state.RegisterEmployeeUiState
import com.performily.flowboard.features.workspace.presentation.state.RegisterField
import java.time.LocalDate

/**
 * Second step of the employee registration: contract, area, position and direct manager.
 *
 * @param state state of the registration form.
 * @param onContractTypeChange called with the selected contract type.
 * @param onHireDateChange called with the hire date.
 * @param onContractEndDateChange called with the contract end date (fixed-term contracts only).
 * @param onAreaChange called with the id of the selected area.
 * @param onPositionChange called with the id of the selected position.
 * @param onDirectManagerChange called with the selected manager, or null for none.
 * @param modifier modifier applied to the step.
 */
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
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

/**
 * Tonal banner with an info icon for a short note of a form.
 *
 * @param text note shown.
 * @param modifier modifier applied to the banner.
 */
@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
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
