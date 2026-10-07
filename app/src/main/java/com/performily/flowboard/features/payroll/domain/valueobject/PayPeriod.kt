package com.performily.flowboard.features.payroll.domain.valueobject

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Mes de planilla al que pertenece una boleta.
 * Reglas (iguales a las del backend): mes entre 1 y 12, año desde 2000 y no puede ser un mes futuro.
 */
data class PayPeriod(val year: Int, val month: Int) {
    init {
        require(month in 1..12) { "El mes debe estar entre 1 y 12." }
        require(year >= MIN_YEAR) { "El año debe ser $MIN_YEAR o posterior." }
        require(!YearMonth.of(year, month).isAfter(YearMonth.now())) { "El período no puede ser futuro." }
    }

    /** Nombre legible del período, por ejemplo "Septiembre 2026". */
    val label: String
        get() {
            val monthName = YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL, SPANISH)
            return monthName.replaceFirstChar { it.titlecase(SPANISH) } + " " + year
        }

    private companion object {
        const val MIN_YEAR = 2000
        val SPANISH: Locale = Locale.forLanguageTag("es-PE")
    }
}
