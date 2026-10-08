package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import javax.inject.Inject


class GetRequestTypesUseCase @Inject constructor(private val repository: RequestTypeRepository) {
    suspend operator fun invoke(activeOnly: Boolean = false): Result<List<RequestType>> =
        repository.getRequestTypes(activeOnly).map { types ->
            types.sortedWith(compareByDescending<RequestType> { it.active }.thenBy { it.id })
        }
}
