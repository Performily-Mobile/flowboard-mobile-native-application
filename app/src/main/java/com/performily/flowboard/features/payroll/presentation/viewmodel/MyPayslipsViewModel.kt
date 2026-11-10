package com.performily.flowboard.features.payroll.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.payroll.application.usecase.GetMyPayslipsUseCase
import com.performily.flowboard.features.payroll.presentation.state.MyPayslipsUiState
import com.performily.flowboard.features.payroll.presentation.state.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the state of the "Boletas" tab: the selected year and its published payslips. */
@HiltViewModel
class MyPayslipsViewModel @Inject constructor(
    private val getMyPayslips: GetMyPayslipsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<MyPayslipsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    /** Loads the payslips when the tab is entered, unless a request is already running. */
    fun load() {
        if (loadJob?.isActive == true) return
        fetch(isRefresh = false)
    }

    /** Reloads the payslips after a pull-to-refresh or a retry. */
    fun refresh() {
        if (loadJob?.isActive == true) return
        fetch(isRefresh = true)
    }

    /**
     * Shows the payslips of another year, dropping the request of the previous one.
     *
     * @param year year picked by the user.
     */
    fun selectYear(year: Int) {
        if (year == _state.value.selectedYear) return
        _state.update { it.copy(selectedYear = year, payslips = emptyList(), errorMessage = null) }
        fetch(isRefresh = false)
    }

    /**
     * Requests the payslips of the selected year, cancelling any request still running.
     *
     * @param isRefresh true when the user asked for the reload explicitly.
     */
    private fun fetch(isRefresh: Boolean) {
        loadJob?.cancel()
        val year = _state.value.selectedYear
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh && it.payslips.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            getMyPayslips(year).fold(
                onSuccess = { payslips ->
                    _state.update {
                        it.copy(payslips = payslips, isLoading = false, isRefreshing = false)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, isRefreshing = false, errorMessage = error.toUserMessage())
                    }
                }
            )
        }
    }

    private fun initialState(): MyPayslipsUiState {
        val currentYear = LocalDate.now().year
        return MyPayslipsUiState(
            years = (currentYear downTo currentYear - YEARS_BACK).toList(),
            selectedYear = currentYear
        )
    }

    private companion object {
        /** Number of previous years offered next to the current one. */
        const val YEARS_BACK = 2
    }
}
