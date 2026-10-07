package com.performily.flowboard.features.request.domain.entity

import java.math.BigDecimal


data class VacationAvailability(
    val employeeId: Long,
    val availableDays: BigDecimal
) {
    fun isEnoughFor(days: Int): Boolean = availableDays >= BigDecimal(days)

    fun afterUsing(days: Int): BigDecimal = availableDays - BigDecimal(days)
}
