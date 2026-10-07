package com.performily.flowboard.features.attendance.presentation.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.attendance.presentation.viewmodel.AttendanceHomeViewModel

@Composable
fun AttendanceHomeScreen(
    onAreaReport: () -> Unit,
    onHoursReport: () -> Unit,
    onJustify: (Long, java.time.LocalDate) -> Unit,
    viewModel: AttendanceHomeViewModel = hiltViewModel()
) {
    when (viewModel.state.role) {
        UserRole.EMPLOYEE -> MyAttendanceScreen(onJustify = onJustify)
        UserRole.HUMAN_RESOURCES -> AttendanceRecordsScreen(
            onAreaReport = onAreaReport,
            onHoursReport = onHoursReport
        )
    }
}
