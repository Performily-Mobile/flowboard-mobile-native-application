package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmployeeService {

    @GET("employees")
    suspend fun getEmployees(
        @Query("search") search: String?,
        @Query("areaId") areaId: Long?,
        @Query("status") status: String?,
        @Query("positionId") positionId: Long?
    ): Response<List<EmployeeDto>>

    @GET("employees/{employeeId}")
    suspend fun getEmployeeById(@Path("employeeId") employeeId: Long): Response<EmployeeDto>

    @POST("employees")
    suspend fun registerEmployee(@Body request: RegisterEmployeeRequestDto): Response<EmployeeDto>

    @GET("employees/organization-chart")
    suspend fun getOrganizationChart(@Query("areaId") areaId: Long?): Response<OrganizationChartDto>
}
