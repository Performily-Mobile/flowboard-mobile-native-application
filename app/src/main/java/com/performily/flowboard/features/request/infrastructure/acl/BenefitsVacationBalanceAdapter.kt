package com.performily.flowboard.features.request.infrastructure.acl

import com.performily.flowboard.features.benefits.application.usecase.GetMyVacationBalanceUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetVacationBalanceUseCase
import com.performily.flowboard.features.request.domain.entity.VacationAvailability
import com.performily.flowboard.features.request.domain.repository.VacationBalanceProvider
import javax.inject.Inject


class BenefitsVacationBalanceAdapter @Inject constructor(
    private val getMyVacationBalance: GetMyVacationBalanceUseCase,
    private val getVacationBalance: GetVacationBalanceUseCase
) : VacationBalanceProvider {

    override suspend fun getMyAvailability(): Result<VacationAvailability> =
        getMyVacationBalance().map { synced ->
            VacationAvailability(employeeId = synced.balance.employeeId, availableDays = synced.balance.availableDays)
        }

    override suspend fun getAvailability(employeeId: Long): Result<VacationAvailability> =
        getVacationBalance(employeeId).map { balance ->
            VacationAvailability(employeeId = balance.employeeId, availableDays = balance.availableDays)
        }
}
