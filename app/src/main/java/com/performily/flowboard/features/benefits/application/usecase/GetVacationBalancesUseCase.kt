package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import javax.inject.Inject

/** Saldos de los colaboradores activos para RR.HH., ordenados por nombre. */
class GetVacationBalancesUseCase @Inject constructor(private val repository: VacationBalanceRepository) {
    suspend operator fun invoke(areaId: Long? = null): Result<List<VacationBalance>> =
        repository.getBalances(areaId).map { list -> list.sortedBy { it.employeeName.orEmpty().lowercase() } }
}
