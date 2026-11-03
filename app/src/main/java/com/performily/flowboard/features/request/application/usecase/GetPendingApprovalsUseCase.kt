package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject


class GetPendingApprovalsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(requestTypeId: Long? = null, page: Int = 0): Result<RequestPage> {
        val pending = when (currentEmployee.currentRole()) {
            UserRole.HUMAN_RESOURCES -> repository.getPendingForHrStaff(requestTypeId, page)
            UserRole.EMPLOYEE -> repository.getPendingForApprover(currentEmployee.currentEmployeeId().value, requestTypeId, page)
        }
        return pending.map { result -> peopleResolver.resolve(result) }
    }
}
