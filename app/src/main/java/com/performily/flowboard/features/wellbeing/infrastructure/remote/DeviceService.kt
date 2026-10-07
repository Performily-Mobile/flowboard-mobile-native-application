package com.performily.flowboard.features.wellbeing.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Endpoints de /api/v1/devices (inventario de dispositivos). */
interface DeviceService {

    @GET("devices")
    suspend fun getDevices(@Query("status") status: String?): Response<List<DeviceDto>>
}
