package com.performily.flowboard.features.payroll.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.repository.MyPayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PayslipDownloadLink
import com.performily.flowboard.features.payroll.infrastructure.mapper.MyPayslipMapper
import com.performily.flowboard.features.payroll.infrastructure.mapper.PayslipMapper
import com.performily.flowboard.features.payroll.infrastructure.remote.MyPayslipService
import javax.inject.Inject

/** Reads the own payslips of the signed-in employee from the backend. */
class MyPayslipRepositoryImpl @Inject constructor(
    private val service: MyPayslipService,
    private val currentEmployee: CurrentEmployeeProvider
) : MyPayslipRepository {

    override suspend fun getMyPayslips(year: Int): Result<List<Payslip>> =
        apiCall { service.getMyPayslips(currentEmployee.currentEmployeeId().value, year) }
            .mapCatching { dtos -> dtos.map { PayslipMapper.toDomain(it) } }

    override suspend fun getMyPayslip(payslipId: Long): Result<PayslipDetail> =
        apiCall { service.getMyPayslip(currentEmployee.currentEmployeeId().value, payslipId) }
            .mapCatching { MyPayslipMapper.toDetail(it) }

    override suspend fun getDownloadLink(payslipId: Long): Result<PayslipDownloadLink> =
        apiCall { service.getDownloadUrl(currentEmployee.currentEmployeeId().value, payslipId) }
            .mapCatching { MyPayslipMapper.toDownloadLink(it) }
}
