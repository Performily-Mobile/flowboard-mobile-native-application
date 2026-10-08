package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject


class GetPendingApprovalsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(requestTypeId: Long? = null): Result<List<Request>> {
        val pending = when (currentEmployee.currentRole()) {
            UserRole.HUMAN_RESOURCES -> repository.getPendingForHrStaff(requestTypeId)
            UserRole.EMPLOYEE -> repository.getPendingForApprover(currentEmployee.currentEmployeeId().value, requestTypeId)
        }
        return pending.map { requests -> peopleResolver.resolve(requests) }
    }
}
