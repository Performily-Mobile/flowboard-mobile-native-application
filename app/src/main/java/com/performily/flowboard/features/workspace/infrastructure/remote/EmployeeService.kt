package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @PUT("employees/{employeeId}")
    suspend fun updatePersonalData(
        @Path("employeeId") employeeId: Long,
        @Body request: UpdateEmployeePersonalDataRequestDto
    ): Response<EmployeeDto>

    @PUT("employees/{employeeId}/direct-manager")
    suspend fun assignDirectManager(
        @Path("employeeId") employeeId: Long,
        @Body request: AssignDirectManagerRequestDto
    ): Response<EmployeeDto>

    @DELETE("employees/{employeeId}/direct-manager")
    suspend fun removeDirectManager(@Path("employeeId") employeeId: Long): Response<EmployeeDto>

    @GET("employees/{employeeId}/subordinates")
    suspend fun getSubordinates(@Path("employeeId") employeeId: Long): Response<List<EmployeeDto>>

    @PATCH("employees/{employeeId}/terminate")
    suspend fun terminate(
        @Path("employeeId") employeeId: Long,
        @Body request: TerminateEmployeeRequestDto
    ): Response<EmployeeDto>

    @PATCH("employees/{employeeId}/reinstate")
    suspend fun reinstate(
        @Path("employeeId") employeeId: Long,
        @Body request: ReinstateEmployeeRequestDto
    ): Response<EmployeeDto>

    @GET("employees/organization-chart")
    suspend fun getOrganizationChart(@Query("areaId") areaId: Long?): Response<OrganizationChartDto>
}
