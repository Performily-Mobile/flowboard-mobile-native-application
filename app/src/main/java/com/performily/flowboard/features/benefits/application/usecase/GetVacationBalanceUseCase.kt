package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import javax.inject.Inject

/** MA-63 · Saldo de un colaborador con sus movimientos. */
class GetVacationBalanceUseCase @Inject constructor(private val repository: VacationBalanceRepository) {
    suspend operator fun invoke(employeeId: Long): Result<VacationBalance> = repository.getBalance(employeeId)
}
