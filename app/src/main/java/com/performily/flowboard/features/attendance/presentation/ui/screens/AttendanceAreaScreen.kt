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
import com.performily.flowboard.core.designsystem.component.FlowboardDateRangeChip
import com.performily.flowboard.core.designsystem.component.FlowboardListRow
import com.performily.flowboard.core.designsystem.component.FlowboardMenuChip
import com.performily.flowboard.core.designsystem.component.FlowboardSectionTitle
import com.performily.flowboard.core.designsystem.component.FlowboardStatusBadge
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.SkeletonList
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.core.text.AppLocale
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmployeeAvatar
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceStatusChip
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSummaryCard
import com.performily.flowboard.features.attendance.presentation.ui.components.SummaryStat
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
                        selectedIndex = 1,
                        onSelected = { index ->
                            when (index) {
                                0 -> onRecords()
                                2 -> onHoursReport()
                            }
                        },
                        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = 4.dp)
                    )
                    FlowboardChipRow {
                        FlowboardMenuChip(
                            name = "Área",
                            selectedLabel = state.selectedArea?.name,
                            options = state.areas,
                            optionLabel = { it.name },
                            onSelect = viewModel::selectArea,
                            icon = FlowboardIcons.AccountTree,
                            showNameWithValue = false
                        )
                        FlowboardDateRangeChip(
                            from = state.period.from,
                            to = state.period.to,
                            onRangeChange = viewModel::setPeriod
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
                        AttendanceSummaryCard(
                            title = "Puntualidad del período",
                            value = "${report.punctualityPercentage}%",
                            caption = "de las marcaciones",
                            progress = report.punctualityPercentage / 100f,
                            stats = listOf(
                                SummaryStat("Tardanzas", report.lateCount.toString()),
                                SummaryStat("Inasistencias", report.absenceCount.toString()),
                                SummaryStat("Horas", formatHours(report.totalEffectiveHours)),
                                SummaryStat("Extra", formatHours(report.totalOvertimeHours))
                            )
                        )
                    }
                    item {
                        FlowboardSectionTitle(
                            title = "Equipo de ${report.areaName}",
                            subtitle = teamSubtitle(report.employees.size),
                            modifier = Modifier.padding(top = Spacing.m)
                        )
                    }
                    items(report.employees, key = { it.employeeId }) { employee ->
                        FlowboardListRow(
                            headline = employee.employeeName,
                            supporting = "${formatHours(employee.effectiveHours)} trabajadas y ${formatHours(employee.overtimeHours)} extra",
                            meta = incidentsLine(employee.lateCount, employee.absenceCount),
                            leading = { AttendanceEmployeeAvatar(employee.employeeName) },
                            trailing = {
                                if (employee.hasRecords) {
                                    AttendanceStatusChip(employee.status)
                                } else {
                                    FlowboardStatusBadge(text = "Sin registros", tone = StatusTone.NEUTRAL)
                                }
                            },
                            onClick = if (employee.hasRecords) {
                                { onEmployeeClick(employee.employeeId, employee.employeeName) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String = String.format(AppLocale, "%.1f h", value)

/**
 * Subtitle of the team section.
 *
 * @param count number of employees of the area.
 * @return text such as "8 colaboradores, toca uno para ver su detalle".
 */
private fun teamSubtitle(count: Int): String =
    "${if (count == 1) "1 colaborador" else "$count colaboradores"}, toca uno para ver su detalle"

/**
 * Line with the late arrivals and absences of an employee, written in full words.
 *
 * @param late number of late arrivals.
 * @param absences number of absences.
 * @return text such as "2 tardanzas y 1 inasistencia", or "Sin incidencias" when both are zero.
 */
private fun incidentsLine(late: Int, absences: Int): String {
    if (late == 0 && absences == 0) return "Sin incidencias"
    val lateText = if (late == 1) "1 tardanza" else "$late tardanzas"
    val absenceText = if (absences == 1) "1 inasistencia" else "$absences inasistencias"
    return "$lateText y $absenceText"
}
