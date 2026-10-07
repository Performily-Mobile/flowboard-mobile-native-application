package com.performily.flowboard.features.payroll.domain.valueobject

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Payroll month a payslip belongs to.
 *
 * Rules (same as the backend): month between 1 and 12 and year 2000 or later. That the period is not
 * a future month is required only when a period is created with [create]; it is not required again when
 * the period is rebuilt from server data, because the clock or time zone of the phone may differ and
 * must not prevent showing data that is already saved.
 *
 * @property year the year of the period
 * @property month the month of the period, from 1 to 12
 */
data class PayPeriod(val year: Int, val month: Int) {
    init {
        require(month in 1..12) { "El mes debe estar entre 1 y 12." }
        require(year >= MIN_YEAR) { "El año debe ser $MIN_YEAR o posterior." }
    }

    /**
     * Readable name of the period, for example "Septiembre 2026".
     */
    val label: String
        get() {
            val monthName = YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL, SPANISH)
            return monthName.replaceFirstChar { it.titlecase(SPANISH) } + " " + year
        }

    /**
     * Readable name in lower case, for example "septiembre 2026", to use inside a sentence.
     */
    val inlineLabel: String get() = label.replaceFirstChar { it.lowercase() }

    companion object {
        private const val MIN_YEAR = 2000
        private val SPANISH: Locale = Locale.forLanguageTag("es-PE")

        /**
         * Creates a new period. Besides the basic rules, it cannot be a future month.
         *
         * @param year the year of the period
         * @param month the month of the period, from 1 to 12
         * @return the period
         * @throws IllegalArgumentException if the month, year or period is not valid
         */
        fun create(year: Int, month: Int): PayPeriod {
            val period = PayPeriod(year, month)
            require(!YearMonth.of(year, month).isAfter(YearMonth.now())) { "El período no puede ser futuro." }
            return period
        }
    }
}
