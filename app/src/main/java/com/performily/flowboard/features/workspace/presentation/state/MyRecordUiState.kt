package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentCategory

data class MyRecordUiState(
    val isLoading: Boolean = false,
    val documents: List<EmployeeDocument> = emptyList(),
    val documentFilter: DocumentCategory? = null,
    val errorMessage: String? = null
) {
    val filteredDocuments: List<EmployeeDocument>
        get() = documentFilter?.let { category -> documents.filter { it.documentType.category == category } }
            ?: documents
}
