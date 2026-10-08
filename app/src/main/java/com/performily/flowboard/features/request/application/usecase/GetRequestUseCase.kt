package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject


class GetRequestUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(requestId: Long): Result<Request> =
        repository.getRequest(requestId).map { peopleResolver.resolve(it) }
}
