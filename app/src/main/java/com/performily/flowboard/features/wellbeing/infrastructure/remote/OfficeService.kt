package com.performily.flowboard.features.wellbeing.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de /api/v1/offices del bounded context Wellbeing. */
interface OfficeService {

    @GET("offices")
    suspend fun getOffices(): Response<List<OfficeStatusDto>>

    @POST("offices")
    suspend fun createOffice(@Body request: CreateOfficeRequestDto): Response<OfficeDto>

    @GET("offices/{officeId}/status")
    suspend fun getOfficeStatus(@Path("officeId") officeId: Long): Response<OfficeStatusDto>

    @POST("offices/{officeId}/devices")
    suspend fun linkDevice(
        @Path("officeId") officeId: Long,
        @Body request: LinkDeviceRequestDto
    ): Response<DeviceDto>

    @DELETE("offices/{officeId}/devices/{deviceCode}")
    suspend fun unlinkDevice(
        @Path("officeId") officeId: Long,
        @Path("deviceCode") deviceCode: String
    ): Response<DeviceDto>

    @GET("offices/{officeId}/thresholds")
    suspend fun getThresholds(@Path("officeId") officeId: Long): Response<List<MetricThresholdDto>>

    @PUT("offices/{officeId}/thresholds/{metricType}")
    suspend fun defineThreshold(
        @Path("officeId") officeId: Long,
        @Path("metricType") metricType: String,
        @Body request: DefineMetricThresholdRequestDto
    ): Response<MetricThresholdDto>

    @GET("offices/{officeId}/readings")
    suspend fun getReadingHistory(
        @Path("officeId") officeId: Long,
        @Query("metricType") metricType: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ReadingHistoryDto>
}
