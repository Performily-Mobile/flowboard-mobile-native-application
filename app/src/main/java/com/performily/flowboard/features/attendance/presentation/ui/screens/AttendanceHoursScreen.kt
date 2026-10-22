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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceHoursViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * HR section "Horas y sobretiempo": effective hours and overtime of each employee of an area.
 *
 * The title, section tabs and filters slide away while the list scrolls down.
 *
 * @param onRecords called when the user opens the "Registros" section.
 * @param onAreaReport called when the user opens the "Resumen por área" section.
 * @param viewModel view model that holds the report and filters.
 */
@Composable
fun AttendanceHoursScreen(
    onRecords: () -> Unit,
    onAreaReport: () -> Unit,
    viewModel: AttendanceHoursViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report
    val headerState = rememberCollapsingHeaderState()

    Scaffold(
        topBar = { FlowboardTopBar(title = "Asistencia") }
    ) { paddingValues ->
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
                    AttendanceSectionTabs(
                        selectedIndex = 2,
                        onSelected = { index ->
                            when (index) {
                                0 -> onRecords()
                                1 -> onAreaReport()
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                        AttendanceDatePickerField(
                            label = "Desde",
                            value = state.period.from,
                            onValueChange = { viewModel.setPeriod(it, state.period.to) },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceDatePickerField(
                            label = "Hasta",
                            value = state.period.to,
                            onValueChange = { viewModel.setPeriod(state.period.from, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    AttendanceFilterDropdown(
                        label = "Área",
                        selectedLabel = state.selectedArea?.name ?: "Selecciona un área",
                        options = state.areas,
                        optionLabel = { it.name },
                        onSelected = viewModel::selectArea,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = state.orderByOvertime,
                        onClick = viewModel::toggleOrder,
                        label = { Text(if (state.orderByOvertime) "Mayor sobretiempo" else "Orden alfabético") }
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
                report == null -> AttendanceEmptyState(
                    title = "Selecciona un área",
                    message = "El reporte se construye con los registros reales de asistencia del período.",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.fillMaxWidth()) {
                            AttendanceMetricCard("Horas efectivas", formatHours(report.totalEffectiveHours), Modifier.weight(1f))
                            AttendanceMetricCard("Sobretiempo", formatHours(report.totalOvertimeHours), Modifier.weight(1f))
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
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
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
                                        "${formatHours(employee.effectiveHours)} efectivas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatHours(employee.overtimeHours), style = MaterialTheme.typography.bodyLarge)
                                    Text("extra", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String = String.format("%.1f h", value)
