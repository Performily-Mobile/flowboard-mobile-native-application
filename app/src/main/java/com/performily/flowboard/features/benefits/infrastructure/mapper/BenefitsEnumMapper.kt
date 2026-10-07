package com.performily.flowboard.features.benefits.infrastructure.mapper

import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.domain.valueobject.VacationMovementType
import java.time.LocalDate
import java.time.LocalDateTime

/** Convierte los textos del backend a enums y fechas sin romper la app si llega un valor desconocido. */
internal object BenefitsEnumMapper {

    fun unit(value: String): BenefitUnit = BenefitUnit.entries.firstOrNull { it.name == value } ?: BenefitUnit.UNITS

    fun status(value: String): AssignmentStatus =
        AssignmentStatus.entries.firstOrNull { it.name == value } ?: AssignmentStatus.ASSIGNED

    fun movementType(value: String): VacationMovementType =
        VacationMovementType.entries.firstOrNull { it.name == value } ?: VacationMovementType.MANUAL_ADJUSTMENT

    fun date(value: String?): LocalDate? = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun dateTime(value: String?): LocalDateTime? = value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
}
