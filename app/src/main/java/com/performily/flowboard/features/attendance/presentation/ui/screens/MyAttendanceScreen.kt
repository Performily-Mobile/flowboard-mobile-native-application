package com.performily.flowboard.features.attendance.presentation.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceInfoBanner
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceRecordItem
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceStatusChip
import com.performily.flowboard.features.attendance.presentation.viewmodel.MyAttendanceViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(
    onJustify: (Long, LocalDate) -> Unit,
    viewModel: MyAttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = state.todayRecord
    val periodLabel = "${state.period.from.format(DateTimeFormatter.ofPattern("dd/MM"))} – ${state.period.to.format(DateTimeFormatter.ofPattern("dd/MM"))}"

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi asistencia") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            if (!LocalDate.now().isBefore(state.period.from) && !LocalDate.now().isAfter(state.period.to)) {
                TodayAttendanceCard(
                    record = today,
                    isPunching = state.isPunching,
                    onPunch = viewModel::punch
                )
                Spacer(Modifier.height(12.dp))
            }

            AttendanceInfoBanner("Las marcaciones se registran en el servidor y se consolidan según la jornada del puesto.")
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
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
            Spacer(Modifier.height(8.dp))
            Text("Historial", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    state.errorMessage != null -> Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                    }
                    state.filteredRecords.isEmpty() -> AttendanceEmptyState(
                        title = "No hay registros en el período seleccionado",
                        message = "Entre el ${state.period.from.format(DateTimeFormatter.ofPattern("dd/MM"))} y el ${state.period.to.format(DateTimeFormatter.ofPattern("dd/MM"))} no se encontraron marcaciones.",
                        actionLabel = "Limpiar filtros",
                        onAction = viewModel::clearFilters,
                        modifier = Modifier.fillMaxWidth().padding(top = 36.dp)
                    )
                    else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(state.filteredRecords, key = { it.id ?: "${it.employeeId}-${it.workDate}" }) { record ->
                            AttendanceRecordItem(
                                record = record,
                                onClick = if (record.status == AttendanceStatus.ABSENT && record.id != null) {
                                    { onJustify(record.id, record.workDate) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayAttendanceCard(
    record: com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord?,
    isPunching: Boolean,
    onPunch: () -> Unit
) {
    androidx.compose.material3.OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Hoy · ${record?.workDate?.format(DateTimeFormatter.ofPattern("EEEE dd/MM", java.util.Locale("es", "PE")))?.replaceFirstChar { it.uppercase() } ?: "sin marcación"}", style = MaterialTheme.typography.titleMedium)
                record?.let { AttendanceStatusChip(it.status) }
            }
            Text(
                when {
                    record?.checkInTime != null && record.checkOutTime == null -> "Entrada marcada a las ${record.checkInTime}"
                    record?.checkInTime != null && record.checkOutTime != null -> "Jornada completada"
                    else -> "Sin marcación de entrada"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                when {
                    record?.checkInTime != null && record.checkOutTime != null -> "${record.checkInTime} – ${record.checkOutTime} · ${record.effectiveHours?.let { String.format("%.1f h", it) } ?: "—"}"
                    record?.checkInTime != null -> "${record.checkInTime} – sin salida"
                    else -> "Horario pendiente de marcación"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val canPunch = record == null || record.checkOutTime == null
            if (canPunch) {
                androidx.compose.material3.Button(
                    onClick = onPunch,
                    enabled = !isPunching,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (record?.checkInTime == null) "Marcar entrada" else "Marcar salida")
                }
            }
        }
    }
}
