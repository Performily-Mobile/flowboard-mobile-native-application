package com.performily.flowboard.features.wellbeing.infrastructure.mapper

import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.infrastructure.remote.DeviceDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.LinkDeviceRequestDto

object DeviceMapper {

    fun toDomain(dto: DeviceDto): Device = Device(
        id = dto.id,
        code = dto.code,
        supportedMetrics = dto.supportedMetrics.orEmpty().mapNotNull(WellbeingEnumMapper::metricType),
        status = WellbeingEnumMapper.deviceStatus(dto.status),
        officeId = dto.officeId,
        lastReadingAt = WellbeingEnumMapper.dateTime(dto.lastReadingAt)
    )

    fun toLinkRequest(deviceCode: String): LinkDeviceRequestDto = LinkDeviceRequestDto(deviceCode = deviceCode)
}
