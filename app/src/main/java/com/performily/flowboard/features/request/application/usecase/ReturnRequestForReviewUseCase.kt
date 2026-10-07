package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject


class ReturnRequestForReviewUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(request: Request, comment: String): Result<Request> {
        val cleanComment = comment.trim()
        return when {
            cleanComment.isEmpty() -> Result.failure(IllegalArgumentException("Ingresa un comentario para el colaborador."))
            cleanComment.length > MAX_COMMENT -> Result.failure(IllegalArgumentException("El comentario admite hasta $MAX_COMMENT caracteres."))
            else -> repository.returnForReview(request.id, currentEmployee.currentEmployeeId().value, cleanComment)
        }
    }

    private companion object {
        const val MAX_COMMENT = 500
    }
}
