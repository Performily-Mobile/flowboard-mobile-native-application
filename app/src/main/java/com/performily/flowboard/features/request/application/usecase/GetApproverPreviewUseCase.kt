package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.domain.entity.RequestPerson
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import javax.inject.Inject


class GetApproverPreviewUseCase @Inject constructor(
    private val directory: RequestEmployeeDirectory,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(): Result<RequestPerson?> =
        directory.getPerson(currentEmployee.currentEmployeeId().value).mapCatching { me ->
            me.directManagerId?.let { directory.getPerson(it).getOrThrow() }
        }
}
