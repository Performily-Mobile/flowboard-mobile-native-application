package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject


class ResubmitRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(
        request: Request,
        type: RequestType?,
        attachments: List<FileReference>,
        comment: String? = null
    ): Result<Request> {
        if (!request.isUnderReview) {
            return Result.failure(IllegalArgumentException("Solo se puede reenviar una solicitud devuelta a revisión."))
        }
        if (type != null) {
            SubmitRequestUseCase.validate(type, request.period, request.fieldValues, attachments)
                ?.let { return Result.failure(IllegalArgumentException(it)) }
        }
        return repository.resubmit(
            requestId = request.id,
            actorId = currentEmployee.currentEmployeeId().value,
            fieldValues = request.fieldValues,
            attachments = attachments,
            comment = comment?.trim()?.takeIf { it.isNotEmpty() }
        )
    }
}
