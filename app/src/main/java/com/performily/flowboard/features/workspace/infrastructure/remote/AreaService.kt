package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AreaService {

    @GET("areas")
    suspend fun getAreas(): Response<List<AreaDto>>

    @POST("areas")
    suspend fun createArea(@Body request: CreateAreaRequestDto): Response<AreaDto>
}
