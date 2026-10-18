package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
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
import java.util.Locale

/** Note about how punches are consolidated, shown once on the screen. */
private const val PUNCH_NOTE = "Las marcaciones se registran en el servidor y se consolidan según la jornada del puesto."

/**
 * Employee view of the Attendance tab: today's punch card and the history of the selected period.
 *
 * Today's card and the filters slide away while the history scrolls down.
 *
 * @param onJustify called with the record id and date of an absence the employee wants to justify.
 * @param viewModel view model that holds the records, filters and punch state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(
    onJustify: (Long, LocalDate) -> Unit,
    viewModel: MyAttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = state.todayRecord
    val shortDate = DateTimeFormatter.ofPattern("dd/MM")
    val showToday = !LocalDate.now().isBefore(state.period.from) && !LocalDate.now().isAfter(state.period.to)
    val headerState = rememberCollapsingHeaderState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi asistencia") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.isLoading, state.errorMessage != null, state.filteredRecords.size),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    if (showToday) {
                        TodayAttendanceCard(
                            record = today,
                            isPunching = state.isPunching,
                            onPunch = viewModel::punch,
                            note = PUNCH_NOTE
                        )
                    } else {
                        AttendanceInfoBanner(PUNCH_NOTE)
                    }
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
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
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
                }
            }
        ) { contentPadding ->
            val stateModifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading -> Box(stateModifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.errorMessage != null -> Column(
                    modifier = stateModifier.padding(top = Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
                }
                state.filteredRecords.isEmpty() -> AttendanceEmptyState(
                    title = "No hay registros en el período seleccionado",
                    message = "Entre el ${state.period.from.format(shortDate)} y el ${state.period.to.format(shortDate)} no se encontraron marcaciones.",
                    actionLabel = "Limpiar filtros",
                    onAction = viewModel::clearFilters,
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
                    item { Text("Historial", style = MaterialTheme.typography.titleMedium) }
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

/**
 * Card with today's punches and the button to punch in or out.
 *
 * @param record today's record, or null when there is no punch yet.
 * @param isPunching true while a punch is being sent.
 * @param onPunch called when the punch button is tapped.
 * @param note short note shown at the bottom of the card.
 */
@Composable
private fun TodayAttendanceCard(
    record: AttendanceRecord?,
    isPunching: Boolean,
    onPunch: () -> Unit,
    note: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Hoy · ${record?.workDate?.format(DateTimeFormatter.ofPattern("EEEE dd/MM", Locale("es", "PE")))?.replaceFirstChar { it.uppercase() } ?: "sin marcación"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                record?.let { AttendanceStatusChip(it.status) }
            }
            Text(
                when {
                    record?.checkInTime != null && record.checkOutTime == null -> "Entrada marcada a las ${record.checkInTime}"
                    record?.checkInTime != null && record.checkOutTime != null -> "Jornada completada"
                    else -> "Sin marcación de entrada"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                when {
                    record?.checkInTime != null && record.checkOutTime != null -> "${record.checkInTime} – ${record.checkOutTime} · ${record.effectiveHours?.let { String.format("%.1f h", it) } ?: "—"}"
                    record?.checkInTime != null -> "${record.checkInTime} – sin salida"
                    else -> "Horario pendiente de marcación"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val canPunch = record == null || record.checkOutTime == null
            if (canPunch) {
                Button(
                    onClick = onPunch,
                    enabled = !isPunching,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(52.dp)
                ) {
                    Text(if (record?.checkInTime == null) "Marcar entrada" else "Marcar salida")
                }
            }
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
