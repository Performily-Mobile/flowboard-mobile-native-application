package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.attendance.presentation.state.AttendanceHomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttendanceHomeViewModel @Inject constructor(
    currentEmployeeProvider: CurrentEmployeeProvider
) : ViewModel() {
    val state = AttendanceHomeUiState(currentEmployeeProvider.currentRole())
}
