package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject

class GetMyRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(): Result<List<Request>> =
        repository.getRequestsOf(currentEmployee.currentEmployeeId().value, status = null)
            .map { requests -> peopleResolver.resolve(requests) }
}
