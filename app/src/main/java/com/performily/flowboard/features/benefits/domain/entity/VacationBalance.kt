package com.performily.flowboard.features.benefits.domain.entity

import com.performily.flowboard.features.benefits.domain.valueobject.VacationMovementType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/** Saldo de vacaciones de un colaborador (US41) con sus movimientos, del más reciente al más antiguo. */
data class VacationBalance(
    val employeeId: Long,
    val employeeName: String?,
    val areaName: String?,
    val accruedDays: BigDecimal,
    val usedDays: BigDecimal,
    val availableDays: BigDecimal,
    val lastAccrualDate: LocalDate?,
    val movements: List<VacationMovement>
) {
    /** Parte usada del total acumulado, de 0 a 1, para la barra de MA-57. */
    val usedRatio: Float
        get() = if (accruedDays.signum() <= 0) 0f
        else (usedDays.toFloat() / accruedDays.toFloat()).coerceIn(0f, 1f)
}

/**
 * Un cambio del saldo.
 * @property days efecto con signo sobre los días disponibles: -3 (uso), +2.5 (acumulación)
 */
data class VacationMovement(
    val id: Long,
    val type: VacationMovementType,
    val days: BigDecimal,
    val reason: String?,
    val authorName: String?,
    val requestId: Long?,
    val occurredAt: LocalDateTime
)

/**
 * Saldo listo para mostrar. Si no hubo conexión se usa la última copia guardada
 * (MA-57) y [syncedAt] dice cuándo se obtuvo.
 */
data class SyncedVacationBalance(
    val balance: VacationBalance,
    val syncedAt: LocalDateTime,
    val fromCache: Boolean
)
