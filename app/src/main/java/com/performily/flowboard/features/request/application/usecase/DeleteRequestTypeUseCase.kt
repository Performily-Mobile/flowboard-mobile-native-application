package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import javax.inject.Inject


class DeleteRequestTypeUseCase @Inject constructor(private val repository: RequestTypeRepository) {
    suspend operator fun invoke(type: RequestType): Result<Unit> = repository.delete(type.id)
}
