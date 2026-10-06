package com.performily.flowboard.features.attendance.domain.valueobject

import java.time.LocalDate

data class AttendancePeriod(
    val from: LocalDate,
    val to: LocalDate
) {
    init {
        require(!to.isBefore(from)) { "La fecha final no puede ser anterior a la fecha inicial." }
    }
}