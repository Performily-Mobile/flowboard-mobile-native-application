package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption
import com.performily.flowboard.features.benefits.domain.repository.WorkspaceDirectory
import javax.inject.Inject

/** MA-61 · Áreas y colaboradores activos para el formulario de asignación. */
class GetAssignmentTargetsUseCase @Inject constructor(private val directory: WorkspaceDirectory) {
    suspend fun areas(): Result<List<AreaOption>> =
        directory.getActiveAreas().map { list -> list.sortedBy { it.name.lowercase() } }

    suspend fun employees(): Result<List<EmployeeOption>> =
        directory.getActiveEmployees().map { list -> list.sortedBy { it.name.lowercase() } }
}
