package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceHomeViewModel

@Composable
fun AttendanceHomeScreen(
    onAreaReport: () -> Unit,
    onEmployeeReport: () -> Unit,
    onHoursReport: () -> Unit,
    onJustify: (Long, java.time.LocalDate) -> Unit,
    viewModel: AttendanceHomeViewModel = hiltViewModel()
) {
    val state = viewModel.state

    when (state.role) {
        UserRole.EMPLOYEE -> MyAttendanceScreen(onJustify = onJustify)
        UserRole.HUMAN_RESOURCES -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("Asistencia", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Consulta los registros operativos y los reportes de horas del personal.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = false, onClick = onAreaReport, label = { Text("Por área") })
                    FilterChip(selected = false, onClick = onEmployeeReport, label = { Text("Colaborador") })
                    FilterChip(selected = false, onClick = onHoursReport, label = { Text("Horas") })
                }
                Text("Selecciona una vista para revisar los datos reales de Attendance.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
