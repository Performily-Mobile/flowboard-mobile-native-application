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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel of the HR benefits screen (MA-60, MA-62).
 *
 * Manages the catalog (US37) and the assignments with delivery registration (US39).
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

    private var catalogJob: Job? = null
    private var assignmentsJob: Job? = null

    /**
     * Loads both tabs.
     *
     * Reloads never hide the data that is already visible.
     */
    fun load() {
        loadCatalog()
        loadAssignments()
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }

    /**
     * Handles the result of the assign benefit screen.
     *
     * Shows the outcome in the assignments tab.
     *
     * @param message the message to show in the snackbar.
     */
    fun onAssignmentResult(message: String) {
        _state.update {
            it.copy(selectedTab = 1, assignmentFilter = AssignmentStatus.ASSIGNED, snackbarMessage = message)
        }
        loadAssignments()
    }

    /**
     * Loads the benefit type catalog (MA-60).
     *
     * When data is already visible, a failure is reported with a snackbar instead of hiding it.
     */
    private fun loadCatalog() {
        catalogJob?.cancel()
        _state.update { it.copy(isLoadingCatalog = it.benefitTypes.isEmpty(), catalogError = null) }
        catalogJob = viewModelScope.launch {
            getBenefitTypes()
                .onSuccess { types -> _state.update { it.copy(isLoadingCatalog = false, benefitTypes = types) } }
                .onFailure { exception ->
                    val message = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudo cargar el catálogo.")
                    _state.update {
                        if (it.benefitTypes.isEmpty()) {
                            it.copy(isLoadingCatalog = false, catalogError = message)
                        } else {
                            it.copy(isLoadingCatalog = false, snackbarMessage = message)
                        }
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

    /**
     * Creates a benefit type.
     *
     * A duplicated name is flagged on the field before calling the backend, as in the prototype.
     */
    fun saveType() {
        val form = _state.value.typeForm
        if (form.isSaving) return
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
        if (_state.value.isTogglingType) return
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

    /**
     * Changes the assignments filter (MA-62) and reloads the list.
     */
    fun onFilterSelected(status: AssignmentStatus) {
        if (status == _state.value.assignmentFilter) return
        _state.update { it.copy(assignmentFilter = status, assignments = emptyList()) }
        loadAssignments()
    }

    /**
     * Loads the assignments for the current filter.
     *
     * A response is ignored when the filter changed while loading. When data is already visible,
     * a failure is reported with a snackbar instead of hiding it.
     */
    private fun loadAssignments() {
        val filter = _state.value.assignmentFilter
        assignmentsJob?.cancel()
        _state.update { it.copy(isLoadingAssignments = it.assignments.isEmpty(), assignmentsError = null) }
        assignmentsJob = viewModelScope.launch {
            getAssignments(filter)
                .onSuccess { list ->
                    if (_state.value.assignmentFilter == filter) {
                        _state.update { it.copy(isLoadingAssignments = false, assignments = list) }
                    }
                }
                .onFailure { exception ->
                    if (_state.value.assignmentFilter == filter) {
                        val message = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudieron cargar las asignaciones.")
                        _state.update {
                            if (it.assignments.isEmpty()) {
                                it.copy(isLoadingAssignments = false, assignmentsError = message)
                            } else {
                                it.copy(isLoadingAssignments = false, snackbarMessage = message)
                            }
                        }
                    }
                }
        }
    }

    /**
     * Opens the delivery dialog for an assignment.
     *
     * A delivery can be neither earlier than the validity start nor in the future, so when the
     * validity has not started yet no valid date exists and a snackbar is shown instead.
     */
    fun showDeliveryDialog(assignment: BenefitAssignment) {
        if (!assignment.canBeDelivered) return
        val today = LocalDate.now()
        if (assignment.startDate.isAfter(today)) {
            _state.update {
                it.copy(
                    snackbarMessage = "La vigencia de este beneficio empieza el ${assignment.startDate.format(DATE_FORMAT)}. " +
                        "Aún no se puede registrar su entrega."
                )
            }
            return
        }
        _state.update { it.copy(deliveryForm = DeliveryForm(assignment = assignment, deliveredOn = today)) }
    }

    fun dismissDeliveryDialog() = _state.update { it.copy(deliveryForm = null) }

    fun onDeliveryDateChange(date: LocalDate) =
        _state.update { it.copy(deliveryForm = it.deliveryForm?.copy(deliveredOn = date, error = null)) }

    fun onDeliveryNotesChange(value: String) =
        _state.update { it.copy(deliveryForm = it.deliveryForm?.copy(notes = value.take(MAX_NOTES))) }

    /**
     * Registers the delivery of the assignment in the open dialog.
     *
     * The POST response lacks the employee name, so the one from the assignment is used.
     */
    fun confirmDelivery() {
        val form = _state.value.deliveryForm ?: return
        if (form.isSaving) return
        _state.update { it.copy(deliveryForm = form.copy(isSaving = true, error = null)) }
        viewModelScope.launch {
            registerDelivery(form.assignment, form.deliveredOn, form.notes)
                .onSuccess { delivered ->
                    _state.update { state ->
                        state.copy(
                            deliveryForm = null,
                            assignments = state.assignments.filterNot { it.id == delivered.id },
                            snackbarMessage = "Entrega registrada: ${form.assignment.benefitTypeName} para ${form.assignment.employeeName}" +
                                " el ${form.deliveredOn.format(DATE_FORMAT)}."
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
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
