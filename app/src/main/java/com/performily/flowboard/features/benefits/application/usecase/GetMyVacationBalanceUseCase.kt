package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import javax.inject.Inject

/** MA-57 · Saldo de vacaciones del colaborador (US41), también sin conexión. */
class GetMyVacationBalanceUseCase @Inject constructor(
    private val repository: VacationBalanceRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(): Result<SyncedVacationBalance> =
        repository.getMyBalance(currentEmployee.currentEmployeeId().value)
}
