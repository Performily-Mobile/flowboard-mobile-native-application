package com.performily.flowboard.features.benefits.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de /api/v1/vacation-balances (US41, US42). */
interface VacationBalanceService {

    @GET("vacation-balances")
    suspend fun getBalances(@Query("areaId") areaId: Long?): Response<List<VacationBalanceDto>>

    @GET("vacation-balances/me")
    suspend fun getMyBalance(@Query("employeeId") employeeId: Long): Response<VacationBalanceDto>

    @GET("vacation-balances/{employeeId}")
    suspend fun getBalance(@Path("employeeId") employeeId: Long): Response<VacationBalanceDto>

    @POST("vacation-balances/{employeeId}/adjustments")
    suspend fun adjust(
        @Path("employeeId") employeeId: Long,
        @Body request: AdjustVacationBalanceRequestDto
    ): Response<VacationBalanceDto>
}
