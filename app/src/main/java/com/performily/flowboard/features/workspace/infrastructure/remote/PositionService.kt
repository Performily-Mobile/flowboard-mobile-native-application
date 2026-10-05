package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PositionService {

    @GET("positions")
    suspend fun getPositions(@Query("areaId") areaId: Long?): Response<List<PositionDto>>

    @POST("positions")
    suspend fun createPosition(@Body request: CreatePositionRequestDto): Response<PositionDto>
}
