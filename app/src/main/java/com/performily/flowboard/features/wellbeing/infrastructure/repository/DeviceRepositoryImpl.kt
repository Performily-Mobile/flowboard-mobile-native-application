package com.performily.flowboard.features.wellbeing.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.repository.DeviceRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.DeviceStatus
import com.performily.flowboard.features.wellbeing.infrastructure.mapper.DeviceMapper
import com.performily.flowboard.features.wellbeing.infrastructure.remote.DeviceService
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeService
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceService: DeviceService,
    private val officeService: OfficeService
) : DeviceRepository {

    override suspend fun getInventoryDevices(): Result<List<Device>> {
        return apiCall { deviceService.getDevices(DeviceStatus.IN_INVENTORY.name) }
            .mapCatching { dtos -> dtos.map { DeviceMapper.toDomain(it) } }
    }

    override suspend fun linkDevice(officeId: Long, deviceCode: String): Result<Device> {
        return apiCall { officeService.linkDevice(officeId, DeviceMapper.toLinkRequest(deviceCode)) }
            .mapCatching { dto -> DeviceMapper.toDomain(dto) }
    }

    override suspend fun unlinkDevice(officeId: Long, deviceCode: String): Result<Device> {
        return apiCall { officeService.unlinkDevice(officeId, deviceCode) }
            .mapCatching { dto -> DeviceMapper.toDomain(dto) }
    }
}
