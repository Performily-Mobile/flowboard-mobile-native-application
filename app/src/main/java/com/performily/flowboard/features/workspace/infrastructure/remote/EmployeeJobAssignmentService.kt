package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EmployeeJobAssignmentService {

    @GET("employees/{employeeId}/job-assignments")
    suspend fun getJobAssignments(@Path("employeeId") employeeId: Long): Response<List<JobAssignmentDto>>

    @POST("employees/{employeeId}/job-assignments")
    suspend fun assignJob(
        @Path("employeeId") employeeId: Long,
        @Body request: AssignJobRequestDto
    ): Response<JobAssignmentDto>
}
