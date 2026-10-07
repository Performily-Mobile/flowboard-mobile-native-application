package com.performily.flowboard.features.request.domain.entity

import java.math.BigDecimal

/**
 * Saldo de vacaciones que Request muestra al pedir y al revisar
 * (MA-38 a MA-41, MA-48). Viene de Benefits.
 */
data class VacationAvailability(
    val employeeId: Long,
    val availableDays: BigDecimal
) {
    fun isEnoughFor(days: Int): Boolean = availableDays >= BigDecimal(days)

    /** Días que quedarían si se aprueba la solicitud. */
    fun afterUsing(days: Int): BigDecimal = availableDays - BigDecimal(days)
}
