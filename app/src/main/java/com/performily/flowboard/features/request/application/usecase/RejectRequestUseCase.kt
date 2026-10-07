package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject

class RejectRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(request: Request, reason: String): Result<Request> {
        val cleanReason = reason.trim()
        return when {
            cleanReason.isEmpty() -> Result.failure(IllegalArgumentException("Ingresa el motivo para rechazar la solicitud."))
            cleanReason.length > MAX_COMMENT -> Result.failure(IllegalArgumentException("El motivo admite hasta $MAX_COMMENT caracteres."))
            else -> repository.reject(request.id, currentEmployee.currentEmployeeId().value, cleanReason)
        }
    }

    private companion object {
        const val MAX_COMMENT = 500
    }
}
