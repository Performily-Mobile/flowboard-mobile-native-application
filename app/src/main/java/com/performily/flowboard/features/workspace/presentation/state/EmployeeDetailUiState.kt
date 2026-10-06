package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import java.time.LocalDate

enum class EmployeeAction {
    NONE,
    REASSIGN_JOB,
    TERMINATE,
    TERMINATION_BLOCKED,
    REINSTATE,
    UPLOAD_DOCUMENT
}

data class ReassignJobForm(
    val areaId: Long? = null,
    val positionId: Long? = null,
    val directManagerId: EmployeeId? = null,
    val effectiveDate: LocalDate = LocalDate.now(),
    val positionError: String? = null,
    val directManagerError: String? = null
)

data class TerminationForm(
    val reason: String? = null,
    val terminationDate: LocalDate? = null,
    val reasonError: String? = null,
    val dateError: String? = null
)

data class ReinstateForm(
    val areaId: Long? = null,
    val positionId: Long? = null,
    val reinstatementDate: LocalDate = LocalDate.now(),
    val areaError: String? = null,
    val positionError: String? = null
)

/** Archivo elegido en el selector del sistema, tal como lo describe el teléfono. */
data class SelectedFile(
    val uri: String,
    val name: String,
    val contentType: String,
    val sizeInBytes: Long
)

data class UploadDocumentForm(
    val documentType: DocumentType? = null,
    val file: SelectedFile? = null,
    val documentTypeError: String? = null,
    val fileError: String? = null
)

data class EmployeeDetailUiState(
    val isLoading: Boolean = false,
    val employee: Employee? = null,
    val directManagerName: String? = null,
    val jobAssignments: List<JobAssignment> = emptyList(),
    val documents: List<EmployeeDocument> = emptyList(),
    val documentFilter: DocumentCategory? = null,
    val selectedTab: Int = 0,
    val errorMessage: String? = null,
    val activeAction: EmployeeAction = EmployeeAction.NONE,
    val areas: List<Area> = emptyList(),
    val positions: List<Position> = emptyList(),
    val managerCandidates: List<Employee> = emptyList(),
    val subordinates: List<Employee> = emptyList(),
    val reassignJobForm: ReassignJobForm = ReassignJobForm(),
    val terminationForm: TerminationForm = TerminationForm(),
    val reinstateForm: ReinstateForm = ReinstateForm(),
    val uploadDocumentForm: UploadDocumentForm = UploadDocumentForm(),
    val isSaving: Boolean = false,
    val actionError: String? = null,
    val message: String? = null
) {
    val filteredDocuments: List<EmployeeDocument>
        get() = documentFilter?.let { category -> documents.filter { it.documentType.category == category } }
            ?: documents

    fun positionsOf(areaId: Long?): List<Position> =
        if (areaId == null) emptyList() else positions.filter { it.belongsTo(areaId) }
}
