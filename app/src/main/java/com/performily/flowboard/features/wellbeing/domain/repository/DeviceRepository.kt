package com.performily.flowboard.features.wellbeing.domain.repository

import com.performily.flowboard.features.wellbeing.domain.entity.Device

interface DeviceRepository {
    suspend fun getInventoryDevices(): Result<List<Device>>
    suspend fun linkDevice(officeId: Long, deviceCode: String): Result<Device>
    suspend fun unlinkDevice(officeId: Long, deviceCode: String): Result<Device>
}
