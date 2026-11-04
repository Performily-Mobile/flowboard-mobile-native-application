package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import javax.inject.Inject

/**
 * Gets one page of the requests of the current employee, newest first.
 */
class GetMyRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    /**
     * @param status status to show, or null for every status.
     * @param page page number, starting at 0.
     */
    suspend operator fun invoke(status: RequestStatus? = null, page: Int = 0): Result<RequestPage> =
        repository.getRequestsOf(currentEmployee.currentEmployeeId().value, status = status, page = page)
            .map { result -> peopleResolver.resolve(result) }
}
