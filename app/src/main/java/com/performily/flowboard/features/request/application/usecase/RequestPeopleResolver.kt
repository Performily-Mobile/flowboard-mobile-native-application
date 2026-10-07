package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestPerson
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import javax.inject.Inject


class RequestPeopleResolver @Inject constructor(
    private val directory: RequestEmployeeDirectory
) {

    suspend fun resolve(requests: List<Request>): List<Request> {
        if (requests.isEmpty()) return requests
        val people = directory.getPeople().getOrNull().orEmpty().associateBy { it.id }
        return requests.map { it.withPeople(people) }
    }

    suspend fun resolve(request: Request): Request = resolve(listOf(request)).first()

    private fun Request.withPeople(people: Map<Long, RequestPerson>): Request = copy(
        requester = people[requesterId],
        approver = approverEmployeeId?.let { people[it] },
        history = history.map { entry -> entry.copy(actorName = entry.actorId?.let { people[it]?.name }) }
    )
}
