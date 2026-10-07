package com.performily.flowboard.features.payroll.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.payroll.application.usecase.GetPaymentStatusReportUseCase
import com.performily.flowboard.features.payroll.application.usecase.GetPayrollAreasUseCase
import com.performily.flowboard.features.payroll.application.usecase.GetPayrollPeriodsUseCase
import com.performily.flowboard.features.payroll.application.usecase.MarkPayslipAsObservedUseCase
import com.performily.flowboard.features.payroll.application.usecase.MarkPayslipAsPaidUseCase
import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.presentation.state.PaymentSheetState
import com.performily.flowboard.features.payroll.presentation.state.PaymentStatusUiState
import com.performily.flowboard.features.payroll.presentation.state.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * View model of the payment status screen (MA-69): payments by period, area and status (HR, US45 and US46).
 */
@HiltViewModel
class PaymentStatusViewModel @Inject constructor(
    private val getPayrollPeriods: GetPayrollPeriodsUseCase,
    private val getPayrollAreas: GetPayrollAreasUseCase,
    private val getPaymentStatusReport: GetPaymentStatusReportUseCase,
    private val markPayslipAsPaid: MarkPayslipAsPaidUseCase,
    private val markPayslipAsObserved: MarkPayslipAsObservedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentStatusUiState())
    val state: StateFlow<PaymentStatusUiState> = _state.asStateFlow()

    private var started = false
    private var reportJob: Job? = null

    /**
     * Starts the screen once, with the period that was selected in MA-67.
     *
     * @param initialPeriodId id of the period to select first, or null to use the most recent one
     */
    fun start(initialPeriodId: Long?) {
        if (started) return
        started = true
        loadPeriods(initialPeriodId)
    }

    /**
     * Loads the areas used by the area filter.
     *
     * A failure is reported with a message and the areas are requested again on the next retry.
     */
    private fun loadAreas() {
        viewModelScope.launch {
            getPayrollAreas()
                .onSuccess { areas -> _state.update { it.copy(areas = areas) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(message = "No se pudieron cargar las áreas: ${exception.toUserMessage()}")
                    }
                }
        }
    }

    /**
     * Loads the payroll periods and then the report of the selected one.
     *
     * Also requests the areas again when they are not loaded yet, so the retry button recovers
     * from a failed first load.
     *
     * @param initialPeriodId id of the period to select, by default the one already selected
     */
    fun loadPeriods(initialPeriodId: Long? = _state.value.selectedPeriod?.id) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        if (_state.value.areas.isEmpty()) loadAreas()
        viewModelScope.launch {
            getPayrollPeriods()
                .onSuccess { periods ->
                    val selected = periods.firstOrNull { it.id == initialPeriodId } ?: periods.firstOrNull()
                    _state.update { it.copy(periods = periods, selectedPeriod = selected) }
                    if (selected != null) loadReport() else _state.update { it.copy(isLoading = false, entries = emptyList()) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.toUserMessage()) }
                }
        }
    }

    fun onPeriodSelected(period: PayrollPeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        loadReport()
    }

    fun onAreaSelected(area: PayrollArea?) {
        _state.update { it.copy(selectedArea = area) }
        loadReport()
    }

    fun onStatusSelected(status: PaymentStatus?) {
        _state.update { it.copy(selectedStatus = status) }
        loadReport()
    }

    fun loadReport() {
        val current = _state.value
        val period = current.selectedPeriod ?: return
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getPaymentStatusReport(period.id, current.selectedArea?.id, current.selectedStatus)
                .onSuccess { entries -> _state.update { it.copy(isLoading = false, errorMessage = null, entries = entries) } }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.toUserMessage()) }
                }
        }
    }

    /**
     * Opens the "Actualizar estado de pago" sheet for a payslip.
     *
     * A pending payslip opens without a selected option; the others show what is already registered.
     *
     * @param entry the payslip entry that was tapped
     */
    fun onEntryClick(entry: PayslipEntry) {
        val payment = entry.payslip.payment
        _state.update {
            it.copy(
                sheet = PaymentSheetState(
                    entry = entry,
                    subtitle = "${entry.fullName} · ${entry.payslip.period.label}",
                    choice = payment.status.takeIf { status -> status != PaymentStatus.PENDING },
                    paidOn = payment.paidOn ?: LocalDate.now(),
                    reason = payment.observationReason.orEmpty()
                )
            )
        }
    }

    fun onSheetChoiceChange(choice: PaymentStatus) = updateSheet { it.copy(choice = choice, errorMessage = null) }

    fun onSheetPaidOnChange(paidOn: LocalDate) = updateSheet { it.copy(paidOn = paidOn, errorMessage = null) }

    fun onSheetReasonChange(reason: String) = updateSheet { it.copy(reason = reason, errorMessage = null) }

    fun onSheetDismiss() {
        if (_state.value.sheet?.isSaving == true) return
        _state.update { it.copy(sheet = null) }
    }

    fun onSheetSave() {
        val sheet = _state.value.sheet ?: return
        if (!sheet.canSave) return
        updateSheet { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val payslip = sheet.entry.payslip
            val result = when (sheet.choice) {
                PaymentStatus.PAID -> markPayslipAsPaid(payslip, requireNotNull(sheet.paidOn))
                PaymentStatus.OBSERVED -> markPayslipAsObserved(payslip, sheet.reason)
                else -> {
                    updateSheet { it.copy(isSaving = false) }
                    return@launch
                }
            }
            result
                .onSuccess {
                    _state.update { it.copy(sheet = null, message = "Estado de pago actualizado.") }
                    loadReport()
                }
                .onFailure { exception ->
                    updateSheet { it.copy(isSaving = false, errorMessage = exception.toUserMessage()) }
                }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun updateSheet(change: (PaymentSheetState) -> PaymentSheetState) {
        _state.update { current -> current.copy(sheet = current.sheet?.let(change)) }
    }
}
