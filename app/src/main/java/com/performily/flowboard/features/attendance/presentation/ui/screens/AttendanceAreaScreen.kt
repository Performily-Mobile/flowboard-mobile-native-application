package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmployeeAvatar
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceFilterDropdown
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceMetricCard
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendancePageHeader
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceAreaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAreaScreen(
    onEmployeeClick: (Long, String) -> Unit,
    onRecords: () -> Unit,
    onHoursReport: () -> Unit,
    viewModel: AttendanceAreaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
        ) {
            AttendancePageHeader(
                subtitle = "Resumen de puntualidad, tardanzas e inasistencias por área."
            )
            AttendanceSectionTabs(
                selectedIndex = 1,
                onSelected = { index ->
                    when (index) {
                        0 -> onRecords()
                        2 -> onHoursReport()
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            AttendanceFilterDropdown(
                label = "Área",
                selectedLabel = state.selectedArea?.name ?: "Selecciona un área",
                options = state.areas,
                optionLabel = { it.name },
                onSelected = viewModel::selectArea,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            AttendanceDatePickerField(
                label = "Período · Desde",
                value = state.period.from,
                onValueChange = { viewModel.setPeriod(it, state.period.to) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            AttendanceDatePickerField(
                label = "Período · Hasta",
                value = state.period.to,
                onValueChange = { viewModel.setPeriod(state.period.from, it) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    state.errorMessage != null -> Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                    }
                    report == null || report.employees.isEmpty() -> AttendanceEmptyState(
                        title = "No hay registros para el período",
                        message = "Selecciona otra área o un rango de fechas con registros.",
                        modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
                    )
                    else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AttendanceMetricCard("Puntualidad", "${report.punctualityPercentage}%", Modifier.weight(1f))
                                AttendanceMetricCard("Tardanzas", report.lateCount.toString(), Modifier.weight(1f))
                                AttendanceMetricCard("Inasistencias", report.absenceCount.toString(), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(12.dp))
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AttendanceMetricCard("Horas efectivas", formatHours(report.totalEffectiveHours), Modifier.weight(1f))
                                AttendanceMetricCard("Sobretiempo", formatHours(report.totalOvertimeHours), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Por colaborador", style = MaterialTheme.typography.titleMedium)
                        }
                        items(report.employees, key = { it.employeeId }) { employee ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEmployeeClick(employee.employeeId, employee.employeeName) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AttendanceEmployeeAvatar(employee.employeeName)
                                    Column(Modifier.weight(1f)) {
                                        Text(employee.employeeName, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            "${formatHours(employee.effectiveHours)} · ${formatHours(employee.overtimeHours)} extra" +
                                                    if (employee.lateCount > 0 || employee.absenceCount > 0) {
                                                        " · ${employee.lateCount} tard. · ${employee.absenceCount} inas."
                                                    } else "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceStatusChip(employee.status)
                                }
                            }
                            androidx.compose.material3.HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String = String.format("%.1f h", value)
