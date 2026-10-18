package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmployeeAvatar
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceFilterDropdown
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceMetricCard
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendancePageHeader
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceStatusChip
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceAreaViewModel

/**
 * HR section "Resumen por área": punctuality, lateness and absences of one area in a period.
 *
 * The title, section tabs and filters slide away while the list scrolls down.
 *
 * @param onEmployeeClick called with the id and name of the employee the user taps.
 * @param onRecords called when the user opens the "Registros" section.
 * @param onHoursReport called when the user opens the "Horas y sobretiempo" section.
 * @param viewModel view model that holds the report and filters.
 */
@Composable
fun AttendanceAreaScreen(
    onEmployeeClick: (Long, String) -> Unit,
    onRecords: () -> Unit,
    onHoursReport: () -> Unit,
    viewModel: AttendanceAreaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report
    val headerState = rememberCollapsingHeaderState()

    Scaffold { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.isLoading, state.errorMessage != null, report?.employees?.size),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    AttendancePageHeader(subtitle = "Resumen de puntualidad, tardanzas e inasistencias por área.")
                    AttendanceSectionTabs(
                        selectedIndex = 1,
                        onSelected = { index ->
                            when (index) {
                                0 -> onRecords()
                                2 -> onHoursReport()
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    AttendanceFilterDropdown(
                        label = "Área",
                        selectedLabel = state.selectedArea?.name ?: "Selecciona un área",
                        options = state.areas,
                        optionLabel = { it.name },
                        onSelected = viewModel::selectArea,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                        AttendanceDatePickerField(
                            label = "Período · Desde",
                            value = state.period.from,
                            onValueChange = { viewModel.setPeriod(it, state.period.to) },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceDatePickerField(
                            label = "Período · Hasta",
                            value = state.period.to,
                            onValueChange = { viewModel.setPeriod(state.period.from, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                report == null || report.employees.isEmpty() -> AttendanceEmptyState(
                    title = "No hay registros para el período",
                    message = "Selecciona otra área o un rango de fechas con registros.",
                    modifier = stateModifier.padding(top = Spacing.xxl)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding() + 8.dp,
                        bottom = Spacing.xl,
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                                AttendanceMetricCard("Puntualidad", "${report.punctualityPercentage}%", Modifier.weight(1f))
                                AttendanceMetricCard("Tardanzas", report.lateCount.toString(), Modifier.weight(1f))
                                AttendanceMetricCard("Inasistencias", report.absenceCount.toString(), Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                                AttendanceMetricCard("Horas efectivas", formatHours(report.totalEffectiveHours), Modifier.weight(1f))
                                AttendanceMetricCard("Sobretiempo", formatHours(report.totalOvertimeHours), Modifier.weight(1f))
                            }
                        }
                    }
                    item {
                        Text(
                            "Por colaborador",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Spacing.m)
                        )
                    }
                    items(report.employees, key = { it.employeeId }) { employee ->
                        val shape = MaterialTheme.shapes.medium
                        Surface(
                            shape = shape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .clickable { onEmployeeClick(employee.employeeId, employee.employeeName) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AttendanceEmployeeAvatar(employee.employeeName)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        employee.employeeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${formatHours(employee.effectiveHours)} · ${formatHours(employee.overtimeHours)} extra" +
                                            if (employee.lateCount > 0 || employee.absenceCount > 0) {
                                                " · ${employee.lateCount} tard. · ${employee.absenceCount} inas."
                                            } else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                AttendanceStatusChip(employee.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String = String.format("%.1f h", value)
