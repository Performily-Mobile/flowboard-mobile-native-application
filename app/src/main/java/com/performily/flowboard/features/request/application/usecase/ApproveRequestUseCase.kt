package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject

class ApproveRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(request: Request): Result<Request> =
        repository.approve(request.id, currentEmployee.currentEmployeeId().value)
}
