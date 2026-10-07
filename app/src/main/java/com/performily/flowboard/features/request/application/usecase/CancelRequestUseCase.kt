package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject

class CancelRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(request: Request): Result<Request> {
        if (request.isResolved) {
            return Result.failure(IllegalArgumentException("La solicitud ya fue resuelta y no se puede cancelar."))
        }
        return repository.cancel(request.id, currentEmployee.currentEmployeeId().value)
    }
}
