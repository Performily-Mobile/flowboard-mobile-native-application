package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee

@Composable
fun EmployeeDropdown(
    label: String,
    employees: List<Employee>,
    selectedEmployeeId: EmployeeId?,
    onSelect: (Employee?) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    val options: List<Employee?> = listOf<Employee?>(null) + employees
    SelectField(
        label = label,
        options = options,
        selected = employees.firstOrNull { it.id == selectedEmployeeId },
        optionLabel = { employee -> employee?.name?.fullName ?: "Sin jefe directo" },
        onSelect = onSelect,
        modifier = modifier,
        supportingText = supportingText
    )
}
