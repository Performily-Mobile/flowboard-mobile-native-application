package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceRecordItem
import com.performily.flowboard.features.attendance.presentation.viewmodel.EmployeeAttendanceViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * Attendance records of one employee, opened from the report by area.
 *
 * The period fields slide away while the list scrolls down.
 *
 * @param employeeId id of the employee.
 * @param employeeName name of the employee, shown until the records load.
 * @param onBack called when the user navigates back.
 * @param viewModel view model that holds the records of the employee.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceEmployeeScreen(
    employeeId: Long,
    employeeName: String,
    onBack: () -> Unit,
    viewModel: EmployeeAttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(employeeId, employeeName) { viewModel.initialize(employeeId, employeeName) }

    val invalidRange = state.period.to.isBefore(state.period.from)
    val headerState = rememberCollapsingHeaderState()

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Asistencia",
                subtitle = state.employeeName.ifBlank { "Colaborador" },
                onBack = onBack
            )
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.isLoading, state.errorMessage != null, state.records.size),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                        AttendanceDatePickerField(
                            label = "Desde",
                            value = state.period.from,
                            onValueChange = { viewModel.setPeriod(it, state.period.to) },
                            modifier = Modifier.weight(1f),
                            isError = invalidRange,
                            supportingText = if (invalidRange) "Debe ser anterior a la fecha de fin." else null
                        )
                        AttendanceDatePickerField(
                            label = "Hasta",
                            value = state.period.to,
                            onValueChange = { viewModel.setPeriod(state.period.from, it) },
                            modifier = Modifier.weight(1f),
                            isError = invalidRange
                        )
                    }
                    if (invalidRange) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Corrige el rango de fechas para ver los registros del período.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Text(
                        "Último período válido · ${state.period.from} al ${state.period.to}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) { contentPadding ->
            val stateModifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading -> Box(stateModifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.errorMessage != null -> Column(
                    modifier = stateModifier.padding(top = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                }
                state.records.isEmpty() -> AttendanceEmptyState(
                    title = "Sin registros",
                    message = "No se encontraron marcaciones para el colaborador en el rango seleccionado.",
                    modifier = stateModifier.padding(top = Spacing.xxl)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding() + 4.dp,
                        bottom = Spacing.xl,
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                ) {
                    items(state.records, key = { it.id ?: "${it.employeeId}-${it.workDate}" }) { record ->
                        AttendanceRecordItem(record = record)
                    }
                }
            }
        }
    }
}
