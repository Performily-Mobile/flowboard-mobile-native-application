package com.performily.flowboard.features.attendance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceWorkspaceService {

    @GET("areas")
    suspend fun getAreas(): Response<List<AreaDto>>

    @GET("employees")
    suspend fun searchEmployees(
        @Query("search") search: String?,
        @Query("areaId") areaId: Long? = null,
        @Query("status") status: String? = "ACTIVE"
    ): Response<List<EmployeeSummaryDto>>

    @GET("employees/{employeeId}")
    suspend fun getEmployee(@Path("employeeId") employeeId: Long): Response<EmployeeSummaryDto>
}
