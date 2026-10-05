package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class GetEmployeeDocumentsUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(employeeId: EmployeeId): Result<List<EmployeeDocument>> {
        return repository.getDocuments(employeeId)
            .map { documents -> documents.sortedByDescending { it.uploadedAt } }
    }
}
