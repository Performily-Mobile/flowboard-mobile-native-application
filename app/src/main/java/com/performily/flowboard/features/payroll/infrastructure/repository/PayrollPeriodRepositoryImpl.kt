package com.performily.flowboard.features.payroll.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.repository.PayrollPeriodRepository
import com.performily.flowboard.features.payroll.infrastructure.mapper.PayrollPeriodMapper
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollPeriodService
import javax.inject.Inject

class PayrollPeriodRepositoryImpl @Inject constructor(
    private val service: PayrollPeriodService
) : PayrollPeriodRepository {

    override suspend fun getPayrollPeriods(): Result<List<PayrollPeriod>> =
        apiCall { service.getPayrollPeriods() }
            .mapCatching { dtos -> dtos.map { PayrollPeriodMapper.toDomain(it) } }

    override suspend fun publishPayslips(payrollPeriodId: Long): Result<Int> =
        apiCall { service.publishPayslips(payrollPeriodId) }
            .mapCatching { it.publishedCount }
}
