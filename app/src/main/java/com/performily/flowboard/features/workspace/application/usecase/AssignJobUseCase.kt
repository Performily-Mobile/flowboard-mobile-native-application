package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import java.time.LocalDate
import javax.inject.Inject

class AssignJobUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(
        id: EmployeeId,
        areaId: Long,
        positionId: Long,
        effectiveDate: LocalDate
    ): Result<JobAssignment> {
        return repository.assignJob(id, areaId, positionId, effectiveDate)
    }
}
