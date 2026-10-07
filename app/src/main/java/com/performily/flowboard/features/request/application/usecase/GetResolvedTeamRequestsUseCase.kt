package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetResolvedTeamRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val directory: RequestEmployeeDirectory,
    private val currentEmployee: CurrentEmployeeProvider,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(): Result<List<Request>> = runCatching {
        val managerId = currentEmployee.currentEmployeeId().value
        val team = directory.getTeam(managerId).getOrThrow()
        val requests = coroutineScope {
            team.map { person -> async { repository.getRequestsOf(person.id, status = null).getOrThrow() } }
                .awaitAll()
                .flatten()
        }
        val resolved = requests
            .filter { it.isResolved && it.approverEmployeeId == managerId }
            .sortedByDescending { it.lastChange?.occurredAt ?: it.submittedAt }
        peopleResolver.resolve(resolved)
    }
}
