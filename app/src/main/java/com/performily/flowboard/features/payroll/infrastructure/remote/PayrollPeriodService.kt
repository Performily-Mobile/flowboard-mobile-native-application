package com.performily.flowboard.features.payroll.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/** Endpoints de PayrollPeriodsController. */
interface PayrollPeriodService {

    @GET("payroll-periods")
    suspend fun getPayrollPeriods(): Response<List<PayrollPeriodDto>>

    @PATCH("payroll-periods/{payrollPeriodId}/publish-payslips")
    suspend fun publishPayslips(@Path("payrollPeriodId") payrollPeriodId: Long): Response<PublishedPayslipsDto>
}
