package com.performily.flowboard.features.payroll.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

private const val EMPLOYEE_HEADER = "X-Employee-Id"

/**
 * Employee endpoints of PayslipsController. The backend still takes the employee from the
 * X-Employee-Id header, so every call sends it. Paths are relative because the base URL already
 * ends with /api/v1/.
 */
interface MyPayslipService {

    @GET("payslips/me")
    suspend fun getMyPayslips(
        @Header(EMPLOYEE_HEADER) employeeId: Long,
        @Query("year") year: Int
    ): Response<List<PayslipDto>>

    @GET("payslips/me/{payslipId}")
    suspend fun getMyPayslip(
        @Header(EMPLOYEE_HEADER) employeeId: Long,
        @Path("payslipId") payslipId: Long
    ): Response<PayslipDetailDto>

    @GET("payslips/me/{payslipId}/download-url")
    suspend fun getDownloadUrl(
        @Header(EMPLOYEE_HEADER) employeeId: Long,
        @Path("payslipId") payslipId: Long
    ): Response<PayslipDownloadUrlDto>
}
