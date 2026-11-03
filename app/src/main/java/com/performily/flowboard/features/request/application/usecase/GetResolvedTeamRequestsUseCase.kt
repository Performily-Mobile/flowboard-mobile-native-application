package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import javax.inject.Inject

/**
 * Gets one page of the requests that the current employee approved or rejected as direct manager.
 */
class GetResolvedTeamRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(page: Int = 0): Result<RequestPage> =
        repository.getAll(
            statuses = listOf(RequestStatus.APPROVED, RequestStatus.REJECTED),
            approverId = currentEmployee.currentEmployeeId().value,
            page = page
        ).map { result -> peopleResolver.resolve(result) }
}
