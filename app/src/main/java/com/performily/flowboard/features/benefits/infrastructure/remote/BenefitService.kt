package com.performily.flowboard.features.benefits.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de /api/v1/benefits: asignaciones (US38), entregas (US39) y mis beneficios (US40). */
interface BenefitService {

    @GET("benefits")
    suspend fun getAssignments(
        @Query("status") status: String?,
        @Query("benefitTypeId") benefitTypeId: Long? = null
    ): Response<List<BenefitAssignmentDto>>

    @GET("benefits/area-preview")
    suspend fun previewAreaAssignment(
        @Query("benefitTypeId") benefitTypeId: Long,
        @Query("areaId") areaId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<AreaAssignmentPreviewDto>

    @GET("benefits/me")
    suspend fun getMyBenefits(@Query("employeeId") employeeId: Long): Response<EmployeeBenefitsDto>

    @POST("benefits")
    suspend fun assign(@Body request: AssignBenefitRequestDto): Response<BenefitAssignmentBatchDto>

    @POST("benefits/{assignmentId}/deliveries")
    suspend fun registerDelivery(
        @Path("assignmentId") assignmentId: Long,
        @Body request: RegisterDeliveryRequestDto
    ): Response<BenefitAssignmentDto>
}
