package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject

class GetMyRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(page: Int = 0): Result<RequestPage> =
        repository.getRequestsOf(currentEmployee.currentEmployeeId().value, status = null, page = page)
            .map { result -> peopleResolver.resolve(result) }
}
