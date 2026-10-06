package com.performily.flowboard.features.attendance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AttendanceWorkspaceService {

    @GET("areas")
    suspend fun getAreas(@Query("onlyActive") onlyActive: Boolean = true): Response<List<AreaDto>>

    @GET("employees")
    suspend fun searchEmployees(
        @Query("search") search: String?,
        @Query("status") status: String? = "ACTIVE"
    ): Response<List<EmployeeSummaryDto>>
}
