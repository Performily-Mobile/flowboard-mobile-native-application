package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.MyPayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayslipDownloadLink
import javax.inject.Inject

/** Gets the temporary link to open the file of a payslip of the signed-in employee. */
class GetMyPayslipDownloadLinkUseCase @Inject constructor(
    private val repository: MyPayslipRepository
) {

    /**
     * @param payslipId identifier of the payslip.
     * @return the temporary link.
     */
    suspend operator fun invoke(payslipId: Long): Result<PayslipDownloadLink> =
        repository.getDownloadLink(payslipId)
}
