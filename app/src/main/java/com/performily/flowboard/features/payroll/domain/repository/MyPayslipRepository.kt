package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.valueobject.PayslipDownloadLink

/**
 * Own payslips of the signed-in employee. Only published payslips are returned.
 */
interface MyPayslipRepository {

    /**
     * Lists the published payslips of the employee for a year, the most recent period first.
     *
     * @param year pay period year.
     * @return the payslips, or a failure when they could not be loaded.
     */
    suspend fun getMyPayslips(year: Int): Result<List<Payslip>>

    /**
     * Gets one payslip of the employee with the employer data.
     *
     * @param payslipId identifier of the payslip.
     * @return the payslip detail, or a failure when it does not exist or is not published.
     */
    suspend fun getMyPayslip(payslipId: Long): Result<PayslipDetail>

    /**
     * Asks for a temporary link to open the file of a payslip.
     *
     * @param payslipId identifier of the payslip.
     * @return the link, or a failure when it could not be generated.
     */
    suspend fun getDownloadLink(payslipId: Long): Result<PayslipDownloadLink>
}
