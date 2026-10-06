package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.application.usecase.AssignDirectManagerUseCase
import com.performily.flowboard.features.workspace.application.usecase.AssignJobUseCase
import com.performily.flowboard.features.workspace.application.usecase.AttachEmployeeDocumentUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeByIdUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeDocumentsUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeesUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetJobAssignmentsUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetPositionsUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetSubordinatesUseCase
import com.performily.flowboard.features.workspace.application.usecase.ReinstateEmployeeUseCase
import com.performily.flowboard.features.workspace.application.usecase.RemoveDirectManagerUseCase
import com.performily.flowboard.features.workspace.application.usecase.TerminateEmployeeUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentUpload
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.TerminationDetails
import com.performily.flowboard.features.workspace.presentation.state.EmployeeAction
import com.performily.flowboard.features.workspace.presentation.state.EmployeeDetailUiState
import com.performily.flowboard.features.workspace.presentation.state.ReassignJobForm
import com.performily.flowboard.features.workspace.presentation.state.ReinstateForm
import com.performily.flowboard.features.workspace.presentation.state.SelectedFile
import com.performily.flowboard.features.workspace.presentation.state.TerminationForm
import com.performily.flowboard.features.workspace.presentation.state.UploadDocumentForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailViewModel @Inject constructor(
    private val getEmployeeById: GetEmployeeByIdUseCase,
    private val getJobAssignments: GetJobAssignmentsUseCase,
    private val getEmployeeDocuments: GetEmployeeDocumentsUseCase,
    private val getAreas: GetAreasUseCase,
    private val getPositions: GetPositionsUseCase,
    private val getEmployees: GetEmployeesUseCase,
    private val getSubordinates: GetSubordinatesUseCase,
    private val assignJob: AssignJobUseCase,
    private val assignDirectManager: AssignDirectManagerUseCase,
    private val removeDirectManager: RemoveDirectManagerUseCase,
    private val terminateEmployee: TerminateEmployeeUseCase,
    private val reinstateEmployee: ReinstateEmployeeUseCase,
    private val attachEmployeeDocument: AttachEmployeeDocumentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeDetailUiState())
    val state: StateFlow<EmployeeDetailUiState> = _state.asStateFlow()

    // ---------- Carga ----------

    fun load(employeeId: Long) {
        val id = EmployeeId(employeeId)
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val assignmentsRequest = async { getJobAssignments(id) }
            val documentsRequest = async { getEmployeeDocuments(id) }

            getEmployeeById(id)
                .onSuccess { employee ->
                    val managerName = employee.directManagerId?.let { managerId ->
                        getEmployeeById(managerId).getOrNull()?.let { manager ->
                            "${manager.name.fullName} · ${manager.positionTitle}"
                        }
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            employee = employee,
                            directManagerName = managerName,
                            jobAssignments = assignmentsRequest.await().getOrDefault(emptyList()),
                            documents = documentsRequest.await().getOrDefault(emptyList())
                        )
                    }
                }
                .onFailure { exception ->
                    assignmentsRequest.cancel()
                    documentsRequest.cancel()
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    private fun reload() {
        _state.value.employee?.let { load(it.id.value) }
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }

    fun onDocumentFilterChange(category: DocumentCategory?) = _state.update { it.copy(documentFilter = category) }

    fun dismissAction() = _state.update { it.copy(activeAction = EmployeeAction.NONE, actionError = null) }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    /** Carga áreas, posiciones y posibles jefes la primera vez que se abre una hoja que los necesita. */
    private fun loadCatalogs() {
        if (_state.value.areas.isNotEmpty()) return
        viewModelScope.launch {
            val areasRequest = async { getAreas(onlyActive = true) }
            val positionsRequest = async { getPositions(onlyActive = true) }
            val employeesRequest = async { getEmployees(status = EmploymentStatus.ACTIVE) }
            val currentId = _state.value.employee?.id
            _state.update {
                it.copy(
                    areas = areasRequest.await().getOrDefault(emptyList()),
                    positions = positionsRequest.await().getOrDefault(emptyList()),
                    managerCandidates = employeesRequest.await().getOrDefault(emptyList())
                        .filterNot { candidate -> candidate.id == currentId }
                )
            }
        }
    }

    // ---------- Reasignar puesto y jefe directo (MA-26) ----------

    fun openReassignJob() {
        val employee = _state.value.employee ?: return
        loadCatalogs()
        _state.update {
            it.copy(
                activeAction = EmployeeAction.REASSIGN_JOB,
                actionError = null,
                reassignJobForm = ReassignJobForm(
                    areaId = employee.areaId,
                    positionId = employee.positionId,
                    directManagerId = employee.directManagerId
                )
            )
        }
    }

    fun onReassignAreaChange(areaId: Long) = _state.update {
        it.copy(reassignJobForm = it.reassignJobForm.copy(areaId = areaId, positionId = null, positionError = null))
    }

    fun onReassignPositionChange(positionId: Long) = _state.update {
        it.copy(reassignJobForm = it.reassignJobForm.copy(positionId = positionId, positionError = null))
    }

    fun onReassignManagerChange(managerId: EmployeeId?) = _state.update {
        it.copy(reassignJobForm = it.reassignJobForm.copy(directManagerId = managerId, directManagerError = null))
    }

    fun onReassignDateChange(date: LocalDate) = _state.update {
        it.copy(reassignJobForm = it.reassignJobForm.copy(effectiveDate = date))
    }

    fun saveReassignJob() {
        val employee = _state.value.employee ?: return
        val form = _state.value.reassignJobForm
        val areaId = form.areaId
        val positionId = form.positionId
        if (areaId == null || positionId == null) {
            _state.update { it.copy(reassignJobForm = form.copy(positionError = "Selecciona una posición.")) }
            return
        }
        val jobChanged = areaId != employee.areaId || positionId != employee.positionId
        val managerChanged = form.directManagerId != employee.directManagerId
        if (!jobChanged && !managerChanged) {
            dismissAction()
            return
        }

        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            // Primero el jefe directo: el backend valida que no se formen ciclos.
            if (managerChanged) {
                val newManager = form.directManagerId
                val result = if (newManager == null) {
                    removeDirectManager(employee.id)
                } else {
                    assignDirectManager(employee.id, newManager)
                }
                result.exceptionOrNull()?.let { exception ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            reassignJobForm = it.reassignJobForm.copy(directManagerError = exception.readableMessage())
                        )
                    }
                    return@launch
                }
            }
            if (jobChanged) {
                assignJob(employee.id, areaId, positionId, form.effectiveDate).exceptionOrNull()?.let { exception ->
                    _state.update { it.copy(isSaving = false, actionError = exception.readableMessage()) }
                    return@launch
                }
            }
            finishAction("Se actualizó el puesto de ${employee.name.firstName}.")
        }
    }

    // ---------- Registrar cese (MA-28) y cese bloqueado (MA-29) ----------

    fun openTerminate() {
        val employee = _state.value.employee ?: return
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            val subordinates = getSubordinates(employee.id).getOrDefault(emptyList())
            _state.update {
                it.copy(
                    isSaving = false,
                    subordinates = subordinates,
                    activeAction = if (subordinates.isEmpty()) {
                        EmployeeAction.TERMINATE
                    } else {
                        EmployeeAction.TERMINATION_BLOCKED
                    },
                    terminationForm = TerminationForm(terminationDate = LocalDate.now())
                )
            }
        }
    }

    fun onTerminationReasonChange(reason: String) = _state.update {
        it.copy(terminationForm = it.terminationForm.copy(reason = reason, reasonError = null))
    }

    fun onTerminationDateChange(date: LocalDate) = _state.update {
        it.copy(terminationForm = it.terminationForm.copy(terminationDate = date, dateError = null))
    }

    fun saveTermination() {
        val employee = _state.value.employee ?: return
        val form = _state.value.terminationForm
        val reason = form.reason
        val date = form.terminationDate
        if (reason.isNullOrBlank() || date == null) {
            _state.update {
                it.copy(
                    terminationForm = form.copy(
                        reasonError = if (reason.isNullOrBlank()) "Selecciona el motivo de cese." else null,
                        dateError = if (date == null) "Selecciona la fecha de cese." else null
                    )
                )
            }
            return
        }
        val details = runCatching { TerminationDetails(reason, date) }.getOrElse { exception ->
            _state.update { it.copy(terminationForm = form.copy(reasonError = exception.message)) }
            return
        }
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            terminateEmployee(employee.id, details)
                .onSuccess { finishAction("${employee.name.fullName} pasó a estado Cesado.") }
                .onFailure { exception ->
                    _state.update { it.copy(isSaving = false, actionError = exception.readableMessage()) }
                }
        }
    }

    // ---------- Reincorporar (MA-30) ----------

    fun openReinstate() {
        val employee = _state.value.employee ?: return
        loadCatalogs()
        _state.update {
            it.copy(
                activeAction = EmployeeAction.REINSTATE,
                actionError = null,
                reinstateForm = ReinstateForm(areaId = employee.areaId, positionId = employee.positionId)
            )
        }
    }

    fun onReinstateAreaChange(areaId: Long) = _state.update {
        it.copy(reinstateForm = it.reinstateForm.copy(areaId = areaId, positionId = null, areaError = null))
    }

    fun onReinstatePositionChange(positionId: Long) = _state.update {
        it.copy(reinstateForm = it.reinstateForm.copy(positionId = positionId, positionError = null))
    }

    fun onReinstateDateChange(date: LocalDate) = _state.update {
        it.copy(reinstateForm = it.reinstateForm.copy(reinstatementDate = date))
    }

    fun saveReinstate() {
        val employee = _state.value.employee ?: return
        val form = _state.value.reinstateForm
        val areaId = form.areaId
        val positionId = form.positionId
        if (areaId == null || positionId == null) {
            _state.update {
                it.copy(
                    reinstateForm = form.copy(
                        areaError = if (areaId == null) "Selecciona un área." else null,
                        positionError = if (positionId == null) "Selecciona una posición." else null
                    )
                )
            }
            return
        }
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            reinstateEmployee(employee.id, areaId, positionId, form.reinstatementDate)
                .onSuccess { finishAction("${employee.name.fullName} fue reincorporado.") }
                .onFailure { exception ->
                    _state.update { it.copy(isSaving = false, actionError = exception.readableMessage()) }
                }
        }
    }

    // ---------- Subir documento (MA-27) ----------

    fun openUploadDocument() = _state.update {
        it.copy(
            activeAction = EmployeeAction.UPLOAD_DOCUMENT,
            actionError = null,
            uploadDocumentForm = UploadDocumentForm()
        )
    }

    fun onUploadDocumentTypeChange(type: DocumentType) = _state.update {
        it.copy(uploadDocumentForm = it.uploadDocumentForm.copy(documentType = type, documentTypeError = null))
    }

    fun onFileSelected(file: SelectedFile) {
        val error = runCatching { file.toUpload() }.exceptionOrNull()?.message
        _state.update { it.copy(uploadDocumentForm = it.uploadDocumentForm.copy(file = file, fileError = error)) }
    }

    fun saveDocument() {
        val employee = _state.value.employee ?: return
        val form = _state.value.uploadDocumentForm
        val type = form.documentType
        val file = form.file
        if (type == null || file == null) {
            _state.update {
                it.copy(
                    uploadDocumentForm = form.copy(
                        documentTypeError = if (type == null) "Selecciona el tipo de documento." else null,
                        fileError = if (file == null) "Selecciona un archivo." else form.fileError
                    )
                )
            }
            return
        }
        val upload = runCatching { file.toUpload() }.getOrElse { exception ->
            _state.update { it.copy(uploadDocumentForm = form.copy(fileError = exception.message)) }
            return
        }
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            attachEmployeeDocument(employee.id, type, upload)
                .onSuccess { finishAction("Documento agregado al expediente.") }
                .onFailure { exception ->
                    _state.update { it.copy(isSaving = false, actionError = exception.readableMessage()) }
                }
        }
    }

    // ---------- Comunes ----------

    private fun finishAction(message: String) {
        _state.update {
            it.copy(isSaving = false, activeAction = EmployeeAction.NONE, actionError = null, message = message)
        }
        reload()
    }

    private fun SelectedFile.toUpload(): DocumentUpload =
        DocumentUpload(sourceUri = uri, fileName = name, contentType = contentType, sizeInBytes = sizeInBytes)

    private fun Throwable.readableMessage(): String = message ?: "No se pudo completar la acción."
}
