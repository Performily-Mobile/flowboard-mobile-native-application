package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod

interface PayrollPeriodRepository {

    /** Períodos de planilla, el más reciente primero. */
    suspend fun getPayrollPeriods(): Result<List<PayrollPeriod>>

    /** Publica todas las boletas del período que siguen en revisión. Devuelve cuántas se publicaron. */
    suspend fun publishPayslips(payrollPeriodId: Long): Result<Int>
}
