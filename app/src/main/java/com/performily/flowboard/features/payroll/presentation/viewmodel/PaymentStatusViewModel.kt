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
import com.performily.flowboard.features.payroll.presentation.ui.components.fullName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** MA-69 · Estado de pagos por período, área y estado (RR.HH., US45 y US46). */
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

    /** Se llama al abrir la pantalla con el período que venía seleccionado en MA-67. */
    fun start(initialPeriodId: Long?) {
        if (started) return
        started = true
        viewModelScope.launch {
            getPayrollAreas().onSuccess { areas -> _state.update { it.copy(areas = areas) } }
        }
        loadPeriods(initialPeriodId)
    }

    fun loadPeriods(initialPeriodId: Long? = _state.value.selectedPeriod?.id) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getPayrollPeriods()
                .onSuccess { periods ->
                    val selected = periods.firstOrNull { it.id == initialPeriodId } ?: periods.firstOrNull()
                    _state.update { it.copy(periods = periods, selectedPeriod = selected) }
                    if (selected != null) loadReport() else _state.update { it.copy(isLoading = false, entries = emptyList()) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
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
                .onSuccess { entries -> _state.update { it.copy(isLoading = false, entries = entries) } }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onEntryClick(entry: PayslipEntry) {
        val payment = entry.payslip.payment
        _state.update {
            it.copy(
                sheet = PaymentSheetState(
                    entry = entry,
                    subtitle = "${entry.fullName} · ${entry.payslip.period.label}",
                    // Una boleta pendiente abre sin opción elegida; las demás muestran lo ya registrado.
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
                else -> return@launch
            }
            result
                .onSuccess {
                    _state.update { it.copy(sheet = null, message = "Estado de pago actualizado.") }
                    loadReport()
                }
                .onFailure { exception ->
                    updateSheet { it.copy(isSaving = false, errorMessage = exception.message) }
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
