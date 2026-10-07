package com.performily.flowboard.features.wellbeing.infrastructure.mapper

import com.performily.flowboard.features.wellbeing.domain.entity.DailyAverage
import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.infrastructure.remote.ReadingHistoryDto
import java.time.LocalDate

object ReadingHistoryMapper {

    fun toDomain(dto: ReadingHistoryDto, requestedMetric: MetricType): ReadingHistory = ReadingHistory(
        officeId = dto.officeId,
        metricType = WellbeingEnumMapper.metricType(dto.metricType) ?: requestedMetric,
        from = LocalDate.parse(dto.from),
        to = LocalDate.parse(dto.to),
        minimum = dto.minimum,
        maximum = dto.maximum,
        average = dto.average,
        daysAboveAcceptable = dto.daysAboveAcceptable,
        dailyAverages = dto.dailyAverages.orEmpty()
            .map { DailyAverage(LocalDate.parse(it.date), it.average, WellbeingEnumMapper.indicator(it.indicator)) }
            .sortedBy { it.date },
        readingsCount = dto.readings.orEmpty().size,
        message = dto.message
    )
}
