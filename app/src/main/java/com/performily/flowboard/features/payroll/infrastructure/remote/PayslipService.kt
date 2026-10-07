package com.performily.flowboard.features.payroll.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de PayslipsController para RR.HH. Rutas relativas: BASE_URL ya termina en /api/v1/. */
interface PayslipService {

    @GET("payslips")
    suspend fun getPayslipsByPayrollPeriod(@Query("payrollPeriodId") payrollPeriodId: Long): Response<List<PayslipDto>>

    @POST("payslips")
    suspend fun uploadPayslip(@Body request: UploadPayslipRequestDto): Response<PayslipDto>

    @PUT("payslips/{payslipId}/file")
    suspend fun replacePayslipFile(
        @Path("payslipId") payslipId: Long,
        @Body request: ReplacePayslipFileRequestDto
    ): Response<PayslipDto>

    @PATCH("payslips/{payslipId}/mark-as-paid")
    suspend fun markAsPaid(
        @Path("payslipId") payslipId: Long,
        @Body request: MarkPayslipAsPaidRequestDto
    ): Response<PayslipDto>

    @PATCH("payslips/{payslipId}/mark-as-observed")
    suspend fun markAsObserved(
        @Path("payslipId") payslipId: Long,
        @Body request: MarkPayslipAsObservedRequestDto
    ): Response<PayslipDto>
}
