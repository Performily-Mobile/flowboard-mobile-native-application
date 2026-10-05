package com.performily.flowboard.features.workspace.domain.valueobject

import java.time.LocalDate
import java.time.Period

@JvmInline
value class BirthDate(val value: LocalDate) {
    init {
        require(!value.isAfter(LocalDate.now())) { "La fecha de nacimiento no puede ser posterior a hoy." }
    }

    fun ageOn(date: LocalDate): Int = Period.between(value, date).years
}
