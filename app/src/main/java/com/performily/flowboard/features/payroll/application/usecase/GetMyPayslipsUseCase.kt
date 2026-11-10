package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.MyPayslipRepository
import javax.inject.Inject

/** Gets the published payslips of the signed-in employee for a year. */
class GetMyPayslipsUseCase @Inject constructor(
    private val repository: MyPayslipRepository
) {

    /**
     * @param year pay period year.
     * @return the payslips, the most recent period first.
     */
    suspend operator fun invoke(year: Int): Result<List<Payslip>> = repository.getMyPayslips(year)
}
