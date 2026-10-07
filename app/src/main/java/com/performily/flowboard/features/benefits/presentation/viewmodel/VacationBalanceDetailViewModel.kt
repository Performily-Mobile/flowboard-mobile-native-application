package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.AdjustVacationBalanceUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetVacationBalanceUseCase
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import com.performily.flowboard.features.benefits.presentation.state.AdjustmentForm
import com.performily.flowboard.features.benefits.presentation.state.VacationBalanceDetailUiState
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** MA-63 · Saldo de vacaciones de un colaborador y su ajuste manual (US42). */
@HiltViewModel
class VacationBalanceDetailViewModel @Inject constructor(
    private val getVacationBalance: GetVacationBalanceUseCase,
    private val adjustVacationBalance: AdjustVacationBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VacationBalanceDetailUiState())
    val state: StateFlow<VacationBalanceDetailUiState> = _state.asStateFlow()

    fun load(employeeId: Long) {
        if (_state.value.employeeId == employeeId && _state.value.balance != null) return
        _state.update { VacationBalanceDetailUiState(employeeId = employeeId, isLoading = true) }
        viewModelScope.launch {
            getVacationBalance(employeeId)
                .onSuccess { balance -> _state.update { it.copy(isLoading = false, balance = balance) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudo cargar el saldo.")
                        )
                    }
                }
        }
    }

    fun retry() {
        val employeeId = _state.value.employeeId ?: return
        _state.update { it.copy(employeeId = null) }
        load(employeeId)
    }

    fun showAdjustSheet() = _state.update { it.copy(isAdjustSheetVisible = true, adjustmentForm = AdjustmentForm()) }

    fun dismissAdjustSheet() = _state.update { it.copy(isAdjustSheetVisible = false) }

    fun onOperationChange(operation: AdjustmentOperation) =
        _state.update { it.copy(adjustmentForm = it.adjustmentForm.copy(operation = operation, daysError = null, errorMessage = null)) }

    fun onDaysChange(value: String) {
        val clean = value.filter { it.isDigit() || it == '.' }
        if (clean.count { it == '.' } > 1) return
        _state.update { it.copy(adjustmentForm = it.adjustmentForm.copy(days = clean, daysError = null, errorMessage = null)) }
    }

    fun onReasonChange(value: String) =
        _state.update { it.copy(adjustmentForm = it.adjustmentForm.copy(reason = value.take(MAX_REASON), reasonError = null)) }

    fun saveAdjustment() {
        val balance = _state.value.balance ?: return
        val form = _state.value.adjustmentForm
        val days = form.days.toBigDecimalOrNull()
        val daysError = if (days == null || days.signum() <= 0) "Ingresa una cantidad mayor que cero." else null
        val reasonError = if (form.reason.isBlank()) "Ingresa el motivo del ajuste." else null
        if (daysError != null || reasonError != null || days == null) {
            _state.update { it.copy(adjustmentForm = form.copy(daysError = daysError, reasonError = reasonError)) }
            return
        }

        _state.update { it.copy(adjustmentForm = form.copy(isSaving = true, errorMessage = null)) }
        viewModelScope.launch {
            adjustVacationBalance(balance, form.operation, days, form.reason)
                .onSuccess { updated ->
                    val signed = if (form.operation == AdjustmentOperation.ADD) days else days.negate()
                    _state.update {
                        it.copy(
                            balance = updated,
                            isAdjustSheetVisible = false,
                            snackbarMessage = "Ajuste guardado: ${BenefitsFormatters.signedDays(signed)}. " +
                                "Disponibles: ${BenefitsFormatters.number(updated.availableDays)}."
                        )
                    }
                }
                .onFailure { exception ->
                    val message = exception.benefitsMessage(BenefitsAction.ADJUST, "No se pudo guardar el ajuste.")
                    _state.update {
                        it.copy(
                            adjustmentForm = it.adjustmentForm.copy(
                                isSaving = false,
                                // Los errores de cantidad se marcan en su campo; el resto, debajo del formulario.
                                daysError = message.takeIf { text -> text.contains("días") || text.contains("cantidad") },
                                errorMessage = message.takeUnless { text -> text.contains("días") || text.contains("cantidad") }
                            )
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private companion object {
        const val MAX_REASON = 250
    }
}
