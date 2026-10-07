package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.ChangeBenefitTypeStatusUseCase
import com.performily.flowboard.features.benefits.application.usecase.CreateBenefitTypeUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetAssignmentsUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetBenefitTypesUseCase
import com.performily.flowboard.features.benefits.application.usecase.RegisterDeliveryUseCase
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.presentation.state.BenefitTypeForm
import com.performily.flowboard.features.benefits.presentation.state.BenefitsAdminUiState
import com.performily.flowboard.features.benefits.presentation.state.DeliveryForm
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * MA-60 / MA-62 · Beneficios de RR.HH.: catálogo (US37) y asignaciones con el
 * registro de entregas (US39).
 */
@HiltViewModel
class BenefitsAdminViewModel @Inject constructor(
    private val getBenefitTypes: GetBenefitTypesUseCase,
    private val createBenefitType: CreateBenefitTypeUseCase,
    private val changeBenefitTypeStatus: ChangeBenefitTypeStatusUseCase,
    private val getAssignments: GetAssignmentsUseCase,
    private val registerDelivery: RegisterDeliveryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BenefitsAdminUiState())
    val state: StateFlow<BenefitsAdminUiState> = _state.asStateFlow()

    /** Carga las dos pestañas. Las recargas no ocultan lo que ya se ve. */
    fun load() {
        loadCatalog()
        loadAssignments()
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }

    /** Al volver de "Asignar beneficio": se muestra el resultado en la pestaña de asignaciones. */
    fun onAssignmentResult(message: String) {
        _state.update {
            it.copy(selectedTab = 1, assignmentFilter = AssignmentStatus.ASSIGNED, snackbarMessage = message)
        }
        loadAssignments()
    }

    // ---------- Catálogo (MA-60) ----------

    private fun loadCatalog() {
        _state.update { it.copy(isLoadingCatalog = it.benefitTypes.isEmpty(), catalogError = null) }
        viewModelScope.launch {
            getBenefitTypes()
                .onSuccess { types -> _state.update { it.copy(isLoadingCatalog = false, benefitTypes = types) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingCatalog = false,
                            catalogError = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudo cargar el catálogo.")
                        )
                    }
                }
        }
    }

    fun showTypeSheet() = _state.update { it.copy(isTypeSheetVisible = true, typeForm = BenefitTypeForm()) }

    fun dismissTypeSheet() = _state.update { it.copy(isTypeSheetVisible = false) }

    fun onTypeNameChange(value: String) =
        _state.update { it.copy(typeForm = it.typeForm.copy(name = value, nameError = null)) }

    fun onTypeUnitChange(unit: BenefitUnit) = _state.update { it.copy(typeForm = it.typeForm.copy(unit = unit)) }

    fun onTypeHasBalanceChange(value: Boolean) =
        _state.update { it.copy(typeForm = it.typeForm.copy(hasBalance = value)) }

    fun saveType() {
        val form = _state.value.typeForm
        // Se avisa en el campo antes de llamar al backend, como en el prototipo.
        val duplicated = _state.value.benefitTypes.any { it.name.equals(form.name.trim(), ignoreCase = true) }
        if (duplicated) {
            _state.update { it.copy(typeForm = form.copy(nameError = "Ya existe un beneficio con este nombre.")) }
            return
        }
        _state.update { it.copy(typeForm = form.copy(isSaving = true)) }
        viewModelScope.launch {
            createBenefitType(form.name, form.unit, form.hasBalance)
                .onSuccess { type ->
                    _state.update {
                        it.copy(isTypeSheetVisible = false, snackbarMessage = "Beneficio \"${type.name}\" creado.")
                    }
                    loadCatalog()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            typeForm = it.typeForm.copy(
                                isSaving = false,
                                nameError = exception.benefitsMessage(BenefitsAction.CREATE_TYPE, "No se pudo crear el beneficio.")
                            )
                        )
                    }
                }
        }
    }

    fun requestToggleType(type: BenefitType) = _state.update { it.copy(typeToToggle = type) }

    fun dismissToggleType() = _state.update { it.copy(typeToToggle = null) }

    fun confirmToggleType() {
        val type = _state.value.typeToToggle ?: return
        _state.update { it.copy(isTogglingType = true) }
        viewModelScope.launch {
            changeBenefitTypeStatus(type)
                .onSuccess { updated ->
                    _state.update { state ->
                        state.copy(
                            isTogglingType = false,
                            typeToToggle = null,
                            benefitTypes = state.benefitTypes.map { if (it.id == updated.id) updated else it },
                            snackbarMessage = if (updated.active) "\"${updated.name}\" activado." else "\"${updated.name}\" desactivado."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isTogglingType = false,
                            typeToToggle = null,
                            snackbarMessage = exception.benefitsMessage(BenefitsAction.CHANGE_TYPE_STATUS, "No se pudo cambiar el estado.")
                        )
                    }
                }
        }
    }

    // ---------- Asignaciones (MA-62) ----------

    fun onFilterSelected(status: AssignmentStatus) {
        if (status == _state.value.assignmentFilter) return
        _state.update { it.copy(assignmentFilter = status, assignments = emptyList()) }
        loadAssignments()
    }

    private fun loadAssignments() {
        val filter = _state.value.assignmentFilter
        _state.update { it.copy(isLoadingAssignments = it.assignments.isEmpty(), assignmentsError = null) }
        viewModelScope.launch {
            getAssignments(filter)
                .onSuccess { list ->
                    // Si el filtro cambió mientras cargaba, se ignora esta respuesta.
                    if (_state.value.assignmentFilter == filter) {
                        _state.update { it.copy(isLoadingAssignments = false, assignments = list) }
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingAssignments = false,
                            assignmentsError = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudieron cargar las asignaciones.")
                        )
                    }
                }
        }
    }

    fun showDeliveryDialog(assignment: BenefitAssignment) {
        if (!assignment.canBeDelivered) return
        val today = LocalDate.now()
        // Por defecto hoy, salvo que la vigencia aún no empiece.
        val defaultDate = if (assignment.startDate.isAfter(today)) assignment.startDate else today
        _state.update { it.copy(deliveryForm = DeliveryForm(assignment = assignment, deliveredOn = defaultDate)) }
    }

    fun dismissDeliveryDialog() = _state.update { it.copy(deliveryForm = null) }

    fun onDeliveryDateChange(date: LocalDate) =
        _state.update { it.copy(deliveryForm = it.deliveryForm?.copy(deliveredOn = date, error = null)) }

    fun onDeliveryNotesChange(value: String) =
        _state.update { it.copy(deliveryForm = it.deliveryForm?.copy(notes = value.take(MAX_NOTES))) }

    fun confirmDelivery() {
        val form = _state.value.deliveryForm ?: return
        _state.update { it.copy(deliveryForm = form.copy(isSaving = true, error = null)) }
        viewModelScope.launch {
            registerDelivery(form.assignment, form.deliveredOn, form.notes)
                .onSuccess { delivered ->
                    _state.update { state ->
                        state.copy(
                            deliveryForm = null,
                            assignments = state.assignments.filterNot { it.id == delivered.id },
                            snackbarMessage = "Entrega registrada: ${delivered.benefitTypeName} para ${delivered.employeeName}" +
                                " el ${BenefitsFormatters.date(form.deliveredOn)}."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            deliveryForm = it.deliveryForm?.copy(
                                isSaving = false,
                                error = exception.benefitsMessage(BenefitsAction.DELIVER, "No se pudo registrar la entrega.")
                            )
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private companion object {
        const val MAX_NOTES = 250
    }
}
