package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
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
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.domain.valueobject.label
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceRecordItem
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceRecordsViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.SkeletonList

/**
 * HR section "Registros": attendance records of the staff, filtered by period, area and status.
 *
 * The title, section tabs and filters slide away while the list scrolls down.
 *
 * @param onAreaReport called when the user opens the "Resumen por área" section.
 * @param onHoursReport called when the user opens the "Horas y sobretiempo" section.
 * @param viewModel view model that holds the records and filters.
 */
@Composable
fun AttendanceRecordsScreen(
    onAreaReport: () -> Unit,
    onHoursReport: () -> Unit,
    viewModel: AttendanceRecordsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredRecords = state.records
    val headerState = rememberCollapsingHeaderState()

    Scaffold(
        topBar = { FlowboardTopBar(title = "Asistencia") }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.isLoading, state.errorMessage != null, filteredRecords.size),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    AttendanceSectionTabs(
                        selectedIndex = 0,
                        onSelected = { index ->
                            when (index) {
                                1 -> onAreaReport()
                                2 -> onHoursReport()
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        FilterChip(
                            selected = state.statusFilter == "TODOS",
                            onClick = { viewModel.setStatusFilter("TODOS") },
                            label = { Text("Todos") }
                        )
                        AttendanceStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.statusFilter == status.name,
                                onClick = { viewModel.setStatusFilter(status.name) },
                                label = { Text(status.label()) }
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            val stateModifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading && state.records.isEmpty() -> SkeletonList(modifier = stateModifier)
                state.errorMessage != null && state.records.isEmpty() -> Column(
                    modifier = stateModifier.padding(top = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                }
                filteredRecords.isEmpty() -> AttendanceEmptyState(
                    title = "No hay registros",
                    message = "No se encontraron registros para los filtros seleccionados.",
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
                        Text(
                            "${filteredRecords.size} de ${state.total} registros",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(
                        filteredRecords,
                        key = { it.id ?: "${it.employeeId}-${it.workDate}" }
                    ) { record ->
                        AttendanceRecordItem(record = record, showEmployee = true)
                    }
                    if (state.hasMore || state.isLoading) {
                        item {
                            OutlinedButton(onClick = viewModel::loadMore, enabled = !state.isLoading) {
                                Text(if (state.isLoading) "Cargando…" else "Cargar más registros")
                            }
                        }
                    }
                }
            }
        }
    }
}