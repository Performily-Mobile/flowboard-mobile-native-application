package com.performily.flowboard.features.wellbeing.infrastructure.mapper

import com.performily.flowboard.features.wellbeing.domain.entity.MetricStatus
import com.performily.flowboard.features.wellbeing.domain.entity.Office
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.OfficeLocation
import com.performily.flowboard.features.wellbeing.infrastructure.remote.CreateOfficeRequestDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.MetricStatusDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeStatusDto

object OfficeMapper {

    fun toDomain(dto: OfficeDto): Office = Office(
        id = dto.id,
        name = dto.name,
        area = dto.area,
        location = OfficeLocation(dto.address, dto.floor, dto.reference),
        active = dto.active
    )

    fun toDomain(dto: OfficeStatusDto): OfficeStatus = OfficeStatus(
        office = Office(
            id = dto.id,
            name = dto.name,
            area = dto.area,
            location = OfficeLocation(dto.address, dto.floor, dto.reference),
            active = dto.active
        ),
        upToDate = dto.upToDate,
        overallIndicator = WellbeingEnumMapper.indicator(dto.overallIndicator),
        lastReadingAt = WellbeingEnumMapper.dateTime(dto.lastReadingAt),
        metrics = dto.metrics.orEmpty()
            .mapNotNull(::toDomain)
            .sortedBy { MetricType.entries.indexOf(it.metricType) },
        devices = dto.devices.orEmpty().map(DeviceMapper::toDomain).sortedBy { it.code }
    )

    private fun toDomain(dto: MetricStatusDto): MetricStatus? {
        val metricType = WellbeingEnumMapper.metricType(dto.metricType) ?: return null
        return MetricStatus(
            metricType = metricType,
            lastValue = dto.lastValue,
            lastRecordedAt = WellbeingEnumMapper.dateTime(dto.lastRecordedAt),
            upToDate = dto.upToDate,
            indicator = WellbeingEnumMapper.indicator(dto.indicator),
            optimalMin = dto.optimalMin,
            optimalMax = dto.optimalMax
        )
    }

    fun toCreateRequest(name: String, area: String?, address: String, floor: String, reference: String?): CreateOfficeRequestDto =
        CreateOfficeRequestDto(name = name, area = area, address = address, floor = floor, reference = reference)
}
