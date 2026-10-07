package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class GetAllRequestsUseCase @Inject constructor(
    private val repository: RequestRepository,
    private val directory: RequestEmployeeDirectory,
    private val peopleResolver: RequestPeopleResolver
) {
    suspend operator fun invoke(): Result<List<Request>> = runCatching {
        val people = directory.getPeople().getOrThrow()
        val requests = coroutineScope {
            people.map { person -> async { repository.getRequestsOf(person.id, status = null).getOrThrow() } }
                .awaitAll()
                .flatten()
        }
        peopleResolver.resolve(requests.sortedByDescending { it.submittedAt })
    }
}
