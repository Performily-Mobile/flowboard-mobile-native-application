package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import javax.inject.Inject

/**
 * Gets one page of the requests of every employee, newest first, for the HR staff.
 */
class GetAllRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val peopleResolver: RequestPeopleResolver
) {
    /**
     * @param status status to show, or null for every status.
     * @param page page number, starting at 0.
     */
    suspend operator fun invoke(status: RequestStatus? = null, page: Int = 0): Result<RequestPage> =
        repository.getAll(statuses = listOfNotNull(status), page = page)
            .map { result -> peopleResolver.resolve(result) }
}
