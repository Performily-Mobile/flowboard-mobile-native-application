package com.performily.flowboard.features.benefits.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de /api/v1/benefit-types (US37). */
interface BenefitTypeService {

    @GET("benefit-types")
    suspend fun getBenefitTypes(@Query("activeOnly") activeOnly: Boolean): Response<List<BenefitTypeDto>>

    @POST("benefit-types")
    suspend fun createBenefitType(@Body request: CreateBenefitTypeRequestDto): Response<BenefitTypeDto>

    @PATCH("benefit-types/{benefitTypeId}/activate")
    suspend fun activate(@Path("benefitTypeId") benefitTypeId: Long): Response<BenefitTypeDto>

    @PATCH("benefit-types/{benefitTypeId}/deactivate")
    suspend fun deactivate(@Path("benefitTypeId") benefitTypeId: Long): Response<BenefitTypeDto>
}
