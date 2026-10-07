package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.GetReadingHistoryUseCase
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.presentation.state.ReadingHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** MA-75 · Histórico de lecturas de un espacio (US51). */
@HiltViewModel
class ReadingHistoryViewModel @Inject constructor(
    private val getReadingHistory: GetReadingHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReadingHistoryUiState())
    val state: StateFlow<ReadingHistoryUiState> = _state.asStateFlow()

    private var request: Job? = null

    fun load(officeId: Long, officeName: String) {
        if (_state.value.officeId == officeId && _state.value.history != null) return
        _state.update { it.copy(officeId = officeId, officeName = officeName) }
        fetch()
    }

    fun onMetricSelected(metricType: MetricType) {
        if (metricType == _state.value.selectedMetric) return
        _state.update { it.copy(selectedMetric = metricType) }
        fetch()
    }

    fun onRangeChange(from: LocalDate, to: LocalDate) {
        _state.update { it.copy(from = from, to = to) }
        fetch()
    }

    fun retry() = fetch()

    private fun fetch() {
        val current = _state.value
        val officeId = current.officeId ?: return
        // Si el usuario cambia de métrica rápido, solo vale la última consulta.
        request?.cancel()
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        request = viewModelScope.launch {
            getReadingHistory(officeId, current.selectedMetric, current.from, current.to)
                .onSuccess { history -> _state.update { it.copy(isLoading = false, history = history) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            history = null,
                            errorMessage = exception.wellbeingMessage("No se pudo cargar el histórico.")
                        )
                    }
                }
        }
    }
}
