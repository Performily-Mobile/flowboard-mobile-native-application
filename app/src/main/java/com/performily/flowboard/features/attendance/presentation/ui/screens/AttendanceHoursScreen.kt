package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardChipRow
import com.performily.flowboard.core.designsystem.component.FlowboardChoiceChip
import com.performily.flowboard.core.designsystem.component.FlowboardDateRangeChip
import com.performily.flowboard.core.designsystem.component.FlowboardListRow
import com.performily.flowboard.core.designsystem.component.FlowboardMenuChip
import com.performily.flowboard.core.designsystem.component.FlowboardSectionTitle
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.SkeletonList
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.text.AppLocale
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmployeeAvatar
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSummaryCard
import com.performily.flowboard.features.attendance.presentation.ui.components.SummaryStat
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceHoursViewModel

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
                        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = 4.dp)
                    )
                    FlowboardChipRow {
                        FlowboardDateRangeChip(
                            from = state.period.from,
                            to = state.period.to,
                            onRangeChange = viewModel::setPeriod
                        )
                        FlowboardMenuChip(
                            name = "Área",
                            selectedLabel = state.selectedArea?.name,
                            options = state.areas,
                            optionLabel = { it.name },
                            onSelect = viewModel::selectArea,
                            icon = FlowboardIcons.AccountTree,
                            showNameWithValue = false
                        )
                        FlowboardChoiceChip(
                            label = "Más horas extra primero",
                            selected = state.orderByOvertime,
                            onClick = viewModel::toggleOrder
                        )
                    }
                }
            }
        ) { contentPadding ->
            val stateModifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading -> SkeletonList(modifier = stateModifier)
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
                        val count = report.employees.size
                        AttendanceSummaryCard(
                            title = "Horas efectivas del período",
                            value = formatHours(report.totalEffectiveHours),
                            stats = listOf(
                                SummaryStat("Sobretiempo", formatHours(report.totalOvertimeHours)),
                                SummaryStat("Colaboradores", count.toString()),
                                SummaryStat("Promedio", formatHours(if (count > 0) report.totalEffectiveHours / count else 0.0))
                            )
                        )
                    }
                    item {
                        FlowboardSectionTitle(
                            title = state.selectedArea?.let { "Equipo de ${it.name}" } ?: "Equipo",
                            subtitle = if (state.orderByOvertime) "Ordenado por más horas extra" else "Ordenado alfabéticamente",
                            modifier = Modifier.padding(top = Spacing.m)
                        )
                    }
                    items(report.employees, key = { it.employeeId }) { employee ->
                        FlowboardListRow(
                            headline = employee.employeeName,
                            supporting = "${formatHours(employee.effectiveHours)} trabajadas",
                            leading = { AttendanceEmployeeAvatar(employee.employeeName) },
                            trailing = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatHours(employee.overtimeHours),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (employee.overtimeHours > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("extra", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String = String.format(AppLocale, "%.1f h", value)
