package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestSortOrder
import javax.inject.Inject

/**
 * Gets one page of the pending requests the current employee has to resolve:
 * as direct manager, or as HR staff when the role is HR.
 */
class GetPendingApprovalsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(
        requestTypeId: Long? = null,
        sort: RequestSortOrder = RequestSortOrder.OLDEST_FIRST,
        page: Int = 0
    ): Result<RequestPage> {
        val pending = when (currentEmployee.currentRole()) {
            UserRole.HUMAN_RESOURCES -> repository.getPendingForHrStaff(requestTypeId, sort, page)
            UserRole.EMPLOYEE ->
                repository.getPendingForApprover(currentEmployee.currentEmployeeId().value, requestTypeId, sort, page)
        }
        return pending.map { result -> peopleResolver.resolve(result) }
    }
}
