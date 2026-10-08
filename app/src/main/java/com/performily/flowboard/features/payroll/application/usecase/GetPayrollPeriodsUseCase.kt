package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.repository.PayrollPeriodRepository
import javax.inject.Inject

class GetPayrollPeriodsUseCase @Inject constructor(
    private val repository: PayrollPeriodRepository
) {

    suspend operator fun invoke(): Result<List<PayrollPeriod>> =
        repository.getPayrollPeriods()
            .map { periods -> periods.sortedWith(compareByDescending<PayrollPeriod> { it.period.year }.thenByDescending { it.period.month }) }
}
