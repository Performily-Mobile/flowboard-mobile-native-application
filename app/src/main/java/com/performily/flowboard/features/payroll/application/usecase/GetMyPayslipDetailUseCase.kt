package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.repository.MyPayslipRepository
import javax.inject.Inject

/** Gets one published payslip of the signed-in employee with the employer data. */
class GetMyPayslipDetailUseCase @Inject constructor(
    private val repository: MyPayslipRepository
) {

    /**
     * @param payslipId identifier of the payslip.
     * @return the payslip detail.
     */
    suspend operator fun invoke(payslipId: Long): Result<PayslipDetail> = repository.getMyPayslip(payslipId)
}
