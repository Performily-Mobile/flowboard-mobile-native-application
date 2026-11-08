package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.presentation.state.AttendanceRecordsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

@HiltViewModel
class AttendanceRecordsViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AttendanceRecordsUiState())
    val state = _state.asStateFlow()

    private var requestJob: Job? = null

    init { load() }

    fun setPeriod(from: LocalDate, to: LocalDate) {
        if (to.isBefore(from)) return
        _state.update { it.copy(period = AttendancePeriod(from, to)) }
        load()
    }

    fun setStatusFilter(status: String) {
        _state.update { it.copy(statusFilter = status) }
        load()
    }

    fun load() {
        requestJob?.cancel()
        _state.update { it.copy(isLoading = false, records = emptyList(), hasMore = false) }
        request(page = 0)
    }
    fun loadMore() {
        val state = _state.value
        if (!state.isLoading && state.hasMore) request(state.nextPage)
    }

    private fun request(page: Int) {
        requestJob = viewModelScope.launch {
            val previous = _state.value
            if (previous.isLoading) return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getRecords(
                previous.period,
                previous.statusFilter.takeUnless { it == "TODOS" },
                page,
                30
            ).onSuccess { result ->
                if (!isActive) return@onSuccess
                _state.update { it.copy(
                    isLoading = false,
                    records = if (page == 0) result.records else it.records + result.records,
                    total = result.total,
                    nextPage = page + 1,
                    hasMore = !result.last
                ) }
            }.onFailure { ex ->
                if (!isActive) return@onFailure
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "No se pudieron cargar los registros") }
            }
        }
    }
}
