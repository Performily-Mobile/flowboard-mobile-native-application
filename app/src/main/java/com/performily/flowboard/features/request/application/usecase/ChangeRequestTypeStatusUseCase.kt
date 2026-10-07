package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import javax.inject.Inject


class ChangeRequestTypeStatusUseCase @Inject constructor(private val repository: RequestTypeRepository) {
    suspend operator fun invoke(type: RequestType): Result<RequestType> =
        if (type.active) repository.deactivate(type.id) else repository.activate(type.id)
}
