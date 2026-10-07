package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.AssignBenefitUseCase
import com.performily.flowboard.features.benefits.application.usecase.AssignmentField
import com.performily.flowboard.features.benefits.application.usecase.GetAssignmentTargetsUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetBenefitTypesUseCase
import com.performily.flowboard.features.benefits.application.usecase.PreviewAreaAssignmentUseCase
import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.AssignmentBatch
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentTarget
import com.performily.flowboard.features.benefits.presentation.state.AssignBenefitUiState
import com.performily.flowboard.features.benefits.presentation.state.AssignTargetMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel of the assign benefit screen (MA-61, US38).
 *
 * When "Área completa" is chosen it requests the preview from the backend to show how many
 * employees will receive the benefit and how many will be skipped.
 */
@HiltViewModel
class AssignBenefitViewModel @Inject constructor(
    private val getBenefitTypes: GetBenefitTypesUseCase,
    private val getTargets: GetAssignmentTargetsUseCase,
    private val previewAreaAssignment: PreviewAreaAssignmentUseCase,
    private val assignBenefit: AssignBenefitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssignBenefitUiState())
    val state: StateFlow<AssignBenefitUiState> = _state.asStateFlow()

    private var previewJob: Job? = null

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val types = async { getBenefitTypes(activeOnly = true) }
            val areas = async { getTargets.areas() }
            val employees = async { getTargets.employees() }
            val typesResult = types.await()
            val areasResult = areas.await()
            val employeesResult = employees.await()
            val error = typesResult.exceptionOrNull() ?: areasResult.exceptionOrNull() ?: employeesResult.exceptionOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    benefitTypes = typesResult.getOrDefault(emptyList()),
                    areas = areasResult.getOrDefault(emptyList()),
                    employees = employeesResult.getOrDefault(emptyList()),
                    loadError = error?.benefitsMessage(BenefitsAction.LOAD, "No se pudieron cargar los datos del formulario.")
                )
            }
        }
    }

    fun onTypeSelected(type: BenefitType) {
        _state.update { it.copy(selectedType = type, quantityError = null, errorMessage = null) }
        refreshPreview()
    }

    fun onModeSelected(mode: AssignTargetMode) {
        _state.update { it.copy(mode = mode, preview = null, previewError = null, errorMessage = null) }
        refreshPreview()
    }

    fun onAreaSelected(area: AreaOption) {
        _state.update { it.copy(selectedArea = area, preview = null, previewError = null, errorMessage = null) }
        refreshPreview()
    }

    fun onEmployeeSelected(employee: EmployeeOption) =
        _state.update { it.copy(selectedEmployee = employee, errorMessage = null) }

    /**
     * Updates the quantity keeping only digits and a single decimal point.
     */
    fun onQuantityChange(value: String) {
        val clean = value.filter { it.isDigit() || it == '.' }
        if (clean.count { it == '.' } > 1) return
        _state.update { it.copy(quantity = clean, quantityError = null, errorMessage = null) }
    }

    /**
     * Updates the start date; when it moves past the end date, the end date moves with it.
     */
    fun onStartDateChange(date: LocalDate) {
        _state.update { state ->
            val end = if (state.endDate.isBefore(date)) date else state.endDate
            state.copy(startDate = date, endDate = end, dateError = null, errorMessage = null)
        }
        refreshPreview()
    }

    /**
     * Updates the end date, which can never be earlier than the start date.
     */
    fun onEndDateChange(date: LocalDate) {
        _state.update { state ->
            val end = if (date.isBefore(state.startDate)) state.startDate else date
            state.copy(endDate = end, dateError = null, errorMessage = null)
        }
        refreshPreview()
    }

    private fun refreshPreview() {
        val current = _state.value
        val type = current.selectedType
        val area = current.selectedArea
        previewJob?.cancel()
        if (current.mode != AssignTargetMode.AREA || type == null || area == null || current.endDate.isBefore(current.startDate)) {
            _state.update { it.copy(preview = null, previewError = null, isLoadingPreview = false) }
            return
        }
        _state.update { it.copy(isLoadingPreview = true, previewError = null) }
        previewJob = viewModelScope.launch {
            previewAreaAssignment(type.id, area.id, current.startDate, current.endDate)
                .onSuccess { preview ->
                    _state.update { it.copy(isLoadingPreview = false, preview = preview, previewError = null) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingPreview = false,
                            preview = null,
                            previewError = exception.benefitsMessage(
                                BenefitsAction.LOAD,
                                "No se pudo calcular cuántos colaboradores lo recibirán."
                            )
                        )
                    }
                }
        }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isSubmitting) return
        val type = current.selectedType ?: return
        val quantity = current.quantity.toBigDecimalOrNull()
        if (quantity == null) {
            _state.update { it.copy(quantityError = "Ingresa una cantidad válida.") }
            return
        }
        val validation = AssignBenefitUseCase.validate(type, quantity, current.startDate, current.endDate)
        if (validation != null) {
            _state.update {
                when (validation.field) {
                    AssignmentField.DATES -> it.copy(dateError = validation.message)
                    AssignmentField.QUANTITY -> it.copy(quantityError = validation.message)
                    AssignmentField.TYPE -> it.copy(errorMessage = validation.message)
                }
            }
            return
        }
        val target: AssignmentTarget = when (current.mode) {
            AssignTargetMode.EMPLOYEE -> AssignmentTarget.Employee(current.selectedEmployee?.id ?: return)
            AssignTargetMode.AREA -> AssignmentTarget.Area(current.selectedArea?.id ?: return)
        }
        val action = if (current.mode == AssignTargetMode.AREA) BenefitsAction.ASSIGN_TO_AREA else BenefitsAction.ASSIGN_TO_EMPLOYEE

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            assignBenefit(type, target, quantity, current.startDate, current.endDate)
                .onSuccess { batch -> _state.update { it.copy(isSubmitting = false, resultMessage = resultMessage(type, batch, current)) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSubmitting = false, errorMessage = exception.benefitsMessage(action, "No se pudo asignar el beneficio."))
                    }
                }
        }
    }

    private fun resultMessage(type: BenefitType, batch: AssignmentBatch, state: AssignBenefitUiState): String {
        if (state.mode == AssignTargetMode.EMPLOYEE) {
            return "${type.name} asignado a ${state.selectedEmployee?.name ?: "el colaborador"}."
        }
        val assigned = "${type.name} asignado a ${batch.assignedCount} " +
            if (batch.assignedCount == 1) "colaborador." else "colaboradores."
        return if (batch.skippedCount > 0) "$assigned ${batch.skippedCount} omitido(s) por ya tenerlo." else assigned
    }
}
