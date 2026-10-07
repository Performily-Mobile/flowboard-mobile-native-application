package com.performily.flowboard.features.request.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface RequestService {

    @POST("requests")
    suspend fun submit(@Body request: SubmitRequestRequestDto): Response<RequestDto>


    @GET("requests/me")
    suspend fun getMyRequests(
        @Query("requesterId") requesterId: Long,
        @Query("status") status: String?
    ): Response<List<RequestDto>>


    @GET("requests/pending-approval")
    suspend fun getPendingForApprover(
        @Query("approverId") approverId: Long,
        @Query("requestTypeId") requestTypeId: Long?
    ): Response<List<RequestDto>>

    @GET("requests/pending-approval/hr-staff")
    suspend fun getPendingForHrStaff(@Query("requestTypeId") requestTypeId: Long?): Response<List<RequestDto>>

    @GET("requests/{requestId}")
    suspend fun getRequest(@Path("requestId") requestId: Long): Response<RequestDto>

    @POST("requests/{requestId}/approve")
    suspend fun approve(@Path("requestId") requestId: Long, @Body request: ResolveRequestRequestDto): Response<RequestDto>

    @POST("requests/{requestId}/reject")
    suspend fun reject(@Path("requestId") requestId: Long, @Body request: ResolveRequestRequestDto): Response<RequestDto>

    @POST("requests/{requestId}/return-for-review")
    suspend fun returnForReview(
        @Path("requestId") requestId: Long,
        @Body request: ResolveRequestRequestDto
    ): Response<RequestDto>

    @POST("requests/{requestId}/resubmit")
    suspend fun resubmit(@Path("requestId") requestId: Long, @Body request: ResubmitRequestRequestDto): Response<RequestDto>

    @POST("requests/{requestId}/cancel")
    suspend fun cancel(@Path("requestId") requestId: Long, @Body request: ResolveRequestRequestDto): Response<RequestDto>
}
