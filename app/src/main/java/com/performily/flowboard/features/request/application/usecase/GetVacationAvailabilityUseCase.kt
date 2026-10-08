package com.performily.flowboard.features.request.application.usecase

import com.performily.flowboard.features.request.domain.entity.VacationAvailability
import com.performily.flowboard.features.request.domain.repository.VacationBalanceProvider
import javax.inject.Inject


class GetVacationAvailabilityUseCase @Inject constructor(
    private val provider: VacationBalanceProvider
) {
    suspend operator fun invoke(employeeId: Long? = null): Result<VacationAvailability> =
        if (employeeId == null) provider.getMyAvailability() else provider.getAvailability(employeeId)
}
