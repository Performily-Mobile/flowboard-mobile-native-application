package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory

data class EmployeeDetailUiState(
    val isLoading: Boolean = false,
    val employee: Employee? = null,
    val directManagerName: String? = null,
    val jobAssignments: List<JobAssignment> = emptyList(),
    val documents: List<EmployeeDocument> = emptyList(),
    val documentFilter: DocumentCategory? = null,
    val selectedTab: Int = 0,
    val errorMessage: String? = null
) {
    val filteredDocuments: List<EmployeeDocument>
        get() = documentFilter?.let { category -> documents.filter { it.documentType.category == category } }
            ?: documents
}
