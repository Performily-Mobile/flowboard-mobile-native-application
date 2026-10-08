package com.performily.flowboard.features.request.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface RequestTypeService {

    @GET("request-types")
    suspend fun getRequestTypes(@Query("activeOnly") activeOnly: Boolean): Response<List<RequestTypeDto>>

    @POST("request-types")
    suspend fun createRequestType(@Body request: CreateRequestTypeRequestDto): Response<RequestTypeDto>

    @PATCH("request-types/{requestTypeId}/activate")
    suspend fun activate(@Path("requestTypeId") requestTypeId: Long): Response<RequestTypeDto>

    @PATCH("request-types/{requestTypeId}/deactivate")
    suspend fun deactivate(@Path("requestTypeId") requestTypeId: Long): Response<RequestTypeDto>

    /** Solo funciona si el tipo no tiene solicitudes; si tiene, hay que desactivarlo (MA-54). */
    @DELETE("request-types/{requestTypeId}")
    suspend fun delete(@Path("requestTypeId") requestTypeId: Long): Response<RequestMessageDto>
}
