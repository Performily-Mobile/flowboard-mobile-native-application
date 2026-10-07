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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceDatePickerField
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceEmptyState
import com.performily.flowboard.features.attendance.presentation.ui.components.AttendanceRecordItem
import com.performily.flowboard.features.attendance.presentation.viewmodel.EmployeeAttendanceViewModel
import java.time.LocalDate

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.employeeName.ifBlank { "Colaborador" })
                        Text("Asistencia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Text("Corrige el rango de fechas para ver los registros del período.", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Último período válido · ${state.period.from} al ${state.period.to}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

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
                    state.records.isEmpty() -> AttendanceEmptyState(
                        title = "Sin registros",
                        message = "No se encontraron marcaciones para el colaborador en el rango seleccionado.",
                        modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
                    )
                    else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(state.records, key = { it.id ?: "${it.employeeId}-${it.workDate}" }) { record ->
                            AttendanceRecordItem(record = record)
                        }
                    }
                }
            }
        }
    }
}
