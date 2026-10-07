package com.performily.flowboard.features.payroll.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.payroll.application.usecase.GetPayrollPeriodsUseCase
import com.performily.flowboard.features.payroll.application.usecase.GetPayslipsByPeriodUseCase
import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.application.usecase.PayslipUploadResult
import com.performily.flowboard.features.payroll.application.usecase.PublishPeriodPayslipsUseCase
import com.performily.flowboard.features.payroll.application.usecase.ReplacePayslipFileUseCase
import com.performily.flowboard.features.payroll.application.usecase.UploadPayslipUseCase
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.valueobject.PayrollSystemFile
import com.performily.flowboard.features.payroll.presentation.state.DuplicatePrompt
import com.performily.flowboard.features.payroll.presentation.state.SelectedPayslipFile
import com.performily.flowboard.features.payroll.presentation.state.UploadPayslipsUiState
import com.performily.flowboard.features.payroll.presentation.state.UploadProgress
import com.performily.flowboard.features.payroll.presentation.ui.components.fullName
import com.performily.flowboard.features.payroll.presentation.ui.components.inlineLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** MA-67 / MA-68 · Carga y publicación de boletas de un período (RR.HH.). */
@HiltViewModel
class UploadPayslipsViewModel @Inject constructor(
    private val getPayrollPeriods: GetPayrollPeriodsUseCase,
    private val getPayslipsByPeriod: GetPayslipsByPeriodUseCase,
    private val uploadPayslip: UploadPayslipUseCase,
    private val replacePayslipFile: ReplacePayslipFileUseCase,
    private val publishPeriodPayslips: PublishPeriodPayslipsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UploadPayslipsUiState())
    val state: StateFlow<UploadPayslipsUiState> = _state.asStateFlow()

    private var payslipsJob: Job? = null
    private var pendingReplaceDecision: CompletableDeferred<Boolean>? = null

    init {
        loadPeriods()
    }

    fun loadPeriods() {
        _state.update { it.copy(isLoadingPeriods = true, errorMessage = null) }
        viewModelScope.launch {
            getPayrollPeriods()
                .onSuccess { periods ->
                    val selected = _state.value.selectedPeriod
                        ?.let { current -> periods.firstOrNull { it.id == current.id } }
                        ?: periods.firstOrNull()
                    _state.update { it.copy(isLoadingPeriods = false, periods = periods, selectedPeriod = selected) }
                    if (selected != null) loadPayslips() else _state.update { it.copy(entries = emptyList()) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoadingPeriods = false, errorMessage = exception.message) }
                }
        }
    }

    fun onPeriodSelected(period: PayrollPeriod) {
        if (period.id == _state.value.selectedPeriod?.id) return
        _state.update { it.copy(selectedPeriod = period, entries = emptyList(), bannerMessage = null) }
        loadPayslips()
    }

    fun loadPayslips() {
        val periodId = _state.value.selectedPeriod?.id ?: return
        payslipsJob?.cancel()
        payslipsJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingPayslips = true, errorMessage = null) }
            getPayslipsByPeriod(periodId)
                .onSuccess { entries -> _state.update { it.copy(isLoadingPayslips = false, entries = entries) } }
                .onFailure { exception ->
                    _state.update { it.copy(isLoadingPayslips = false, errorMessage = exception.message) }
                }
        }
    }

    /**
     * Valida y carga los archivos elegidos, uno por uno. Si un colaborador ya tiene boleta
     * en el período, se detiene y pregunta si se reemplaza (MA-68).
     */
    fun onFilesSelected(files: List<SelectedPayslipFile>) {
        val period = _state.value.selectedPeriod ?: return
        if (_state.value.isUploading || files.isEmpty()) return

        val errors = mutableListOf<String>()
        val valid = files.mapNotNull { selected ->
            PayrollSystemFile.from(selected.uri, selected.name, selected.contentType, selected.sizeInBytes)
                .onFailure { errors += it.message.orEmpty() }
                .getOrNull()
        }
        _state.update {
            it.copy(
                bannerMessage = errors.toBannerMessage(),
                upload = if (valid.isEmpty()) null else UploadProgress(current = 0, total = valid.size)
            )
        }
        if (valid.isEmpty()) return

        viewModelScope.launch {
            val known = _state.value.entries.map { it.payslip }.toMutableList()
            var uploaded = 0
            var replaced = 0
            valid.forEachIndexed { index, file ->
                _state.update { it.copy(upload = UploadProgress(current = index + 1, total = valid.size)) }
                uploadPayslip(period.id, file, known)
                    .onSuccess { result ->
                        when (result) {
                            is PayslipUploadResult.Uploaded -> {
                                known += result.payslip
                                uploaded++
                            }
                            is PayslipUploadResult.Duplicate -> {
                                if (askReplace(result.existing, period, file)) {
                                    replacePayslipFile(result.existing, file)
                                        .onSuccess { updated ->
                                            val index = known.indexOfFirst { it.id == updated.id }
                                            if (index >= 0) known[index] = updated
                                            replaced++
                                        }
                                        .onFailure { errors += "${file.fileName}: ${it.message}" }
                                }
                            }
                        }
                    }
                    .onFailure { errors += "${file.fileName}: ${it.message}" }
            }
            _state.update {
                it.copy(upload = null, bannerMessage = errors.toBannerMessage(), message = summary(uploaded, replaced))
            }
            loadPayslips()
        }
    }

    fun onReplaceConfirmed() = resolveDuplicate(replace = true)

    fun onReplaceCancelled() = resolveDuplicate(replace = false)

    fun onPublishClick() {
        val period = _state.value.selectedPeriod ?: return
        if (!_state.value.canPublish) return
        _state.update { it.copy(isPublishing = true, bannerMessage = null) }
        viewModelScope.launch {
            publishPeriodPayslips(period.id)
                .onSuccess { count ->
                    val message = if (count == 1) "Se publicó 1 boleta." else "Se publicaron $count boletas."
                    _state.update { it.copy(isPublishing = false, message = message) }
                    loadPayslips()
                }
                .onFailure { exception ->
                    _state.update { it.copy(isPublishing = false, message = exception.message) }
                }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private suspend fun askReplace(existing: Payslip, period: PayrollPeriod, file: PayrollSystemFile): Boolean {
        val entry = _state.value.entries.firstOrNull { it.payslip.employeeId == existing.employeeId }
            ?: PayslipEntry(existing, employee = null)
        val decision = CompletableDeferred<Boolean>()
        pendingReplaceDecision = decision
        _state.update {
            it.copy(
                duplicate = DuplicatePrompt(
                    employeeName = entry.fullName,
                    periodLabel = period.period.inlineLabel,
                    fileName = file.fileName
                )
            )
        }
        return decision.await()
    }

    private fun resolveDuplicate(replace: Boolean) {
        _state.update { it.copy(duplicate = null) }
        pendingReplaceDecision?.complete(replace)
        pendingReplaceDecision = null
    }

    private fun List<String>.toBannerMessage(): String? =
        filter { it.isNotBlank() }.joinToString("\n").ifEmpty { null }

    private fun summary(uploaded: Int, replaced: Int): String? {
        val parts = buildList {
            if (uploaded > 0) add(if (uploaded == 1) "1 boleta cargada" else "$uploaded boletas cargadas")
            if (replaced > 0) add(if (replaced == 1) "1 boleta reemplazada" else "$replaced boletas reemplazadas")
        }
        return parts.joinToString(" · ").ifEmpty { null }
    }

    override fun onCleared() {
        pendingReplaceDecision?.complete(false)
        super.onCleared()
    }
}
