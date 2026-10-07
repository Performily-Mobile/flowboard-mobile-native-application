package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.domain.valueobject.label
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceFilterDropdown
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendancePageHeader
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceRecordItem
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceSectionTabs
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceAreaViewModel

@Composable
fun AttendanceRecordsScreen(
    onAreaReport: () -> Unit,
    onHoursReport: () -> Unit,
    viewModel: AttendanceAreaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val records = state.report?.records.orEmpty()
    val filteredRecords = if (state.statusFilter == "TODOS") records else records.filter { it.status.name == state.statusFilter }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AttendancePageHeader(
                subtitle = "Consulta los registros de asistencia del personal y sus marcaciones."
            )
            AttendanceSectionTabs(
                selectedIndex = 0,
                onSelected = { index ->
                    when (index) {
                        1 -> onAreaReport()
                        2 -> onHoursReport()
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
            Spacer(Modifier.height(8.dp))
            AttendanceFilterDropdown(
                label = "Área",
                selectedLabel = state.selectedArea?.name ?: "Selecciona un área",
                options = state.areas,
                optionLabel = { it.name },
                onSelected = viewModel::selectArea,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            Spacer(Modifier.height(10.dp))

            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.errorMessage != null -> Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                    }
                    filteredRecords.isEmpty() -> AttendanceEmptyState(
                        title = "No hay registros",
                        message = "No se encontraron registros para los filtros seleccionados.",
                        modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
                    )
                    else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        item {
                            Text(
                                "${filteredRecords.size} registros",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(
                            filteredRecords.sortedByDescending { it.workDate },
                            key = { it.id ?: "${it.employeeId}-${it.workDate}" }
                        ) { record ->
                            AttendanceRecordItem(record = record, showEmployee = true)
                        }
                    }
                }
            }
        }
    }
}
