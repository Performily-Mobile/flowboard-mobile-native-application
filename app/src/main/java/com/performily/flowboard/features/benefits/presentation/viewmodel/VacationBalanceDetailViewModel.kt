package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.AdjustVacationBalanceUseCase
import com.performily.flowboard.features.benefits.application.usecase.AdjustmentField
import com.performily.flowboard.features.benefits.application.usecase.AdjustmentValidationException
import com.performily.flowboard.features.benefits.application.usecase.GetVacationBalanceUseCase
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import com.performily.flowboard.features.benefits.presentation.state.AdjustmentForm
import com.performily.flowboard.features.benefits.presentation.state.VacationBalanceDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel of the vacation balance detail (MA-63, US42).
 *
 * Shows the balance of an employee and handles its manual adjustment.
 */
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

    /**
     * Saves the manual adjustment.
     *
     * The adjustment rules (quantity, reason, enough balance) are validated by the use case. The
     * POST response lacks names (employee, area and movement authors), so the balance is fetched
     * again and, if that fails, the names are completed from the previous balance. Validation
     * errors are flagged on their field; any other error is shown below the form.
     */
    fun saveAdjustment() {
        val balance = _state.value.balance ?: return
        val form = _state.value.adjustmentForm
        if (form.isSaving) return
        val days = form.days.toBigDecimalOrNull()
        if (days == null) {
            _state.update { it.copy(adjustmentForm = form.copy(daysError = "Ingresa una cantidad válida.")) }
            return
        }

        _state.update { it.copy(adjustmentForm = form.copy(isSaving = true, daysError = null, reasonError = null, errorMessage = null)) }
        viewModelScope.launch {
            adjustVacationBalance(balance, form.operation, days, form.reason)
                .onSuccess { adjusted ->
                    val refreshed = getVacationBalance(balance.employeeId).getOrNull()
                    val updated = refreshed ?: withKnownNames(adjusted, balance)
                    val signed = if (form.operation == AdjustmentOperation.ADD) days else days.negate()
                    _state.update {
                        it.copy(
                            balance = updated,
                            isAdjustSheetVisible = false,
                            snackbarMessage = "Ajuste guardado: ${signedDaysText(signed)}. " +
                                "Disponibles: ${numberText(updated.availableDays)}."
                        )
                    }
                }
                .onFailure { exception ->
                    val message = exception.benefitsMessage(BenefitsAction.ADJUST, "No se pudo guardar el ajuste.")
                    val field = (exception as? AdjustmentValidationException)?.field
                    _state.update {
                        it.copy(
                            adjustmentForm = it.adjustmentForm.copy(
                                isSaving = false,
                                daysError = message.takeIf { field == AdjustmentField.DAYS },
                                reasonError = message.takeIf { field == AdjustmentField.REASON },
                                errorMessage = message.takeIf { field == null }
                            )
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private fun withKnownNames(adjusted: VacationBalance, previous: VacationBalance): VacationBalance {
        val authors = previous.movements.associate { it.id to it.authorName }
        return adjusted.copy(
            employeeName = adjusted.employeeName ?: previous.employeeName,
            areaName = adjusted.areaName ?: previous.areaName,
            movements = adjusted.movements.map { movement ->
                movement.copy(authorName = movement.authorName ?: authors[movement.id])
            }
        )
    }

    /**
     * Formats a number without trailing zeros.
     *
     * Examples: "2.5", "3".
     */
    private fun numberText(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()

    /**
     * Formats a signed number of days.
     *
     * Examples: "+2.5 días", "−3 días", "+1 día".
     */
    private fun signedDaysText(value: BigDecimal): String {
        val sign = if (value.signum() < 0) "−" else "+"
        val abs = value.abs()
        return "$sign${numberText(abs)} ${if (abs.compareTo(BigDecimal.ONE) == 0) "día" else "días"}"
    }

    private companion object {
        const val MAX_REASON = 250
    }
}
