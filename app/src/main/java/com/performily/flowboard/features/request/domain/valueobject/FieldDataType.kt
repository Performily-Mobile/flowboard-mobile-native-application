package com.performily.flowboard.features.request.domain.valueobject

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime


enum class FieldDataType(val label: String) {
    TEXT("Texto"),
    NUMBER("Número"),
    DATE("Fecha"),
    TIME("Hora"),
    MONEY("Monto"),
    BOOLEAN("Sí / No");

    fun isValid(value: String): Boolean {
        val text = value.trim()
        return runCatching {
            when (this) {
                TEXT -> true
                NUMBER -> {
                    BigDecimal(text)
                    true
                }
                DATE -> {
                    LocalDate.parse(text)
                    true
                }
                TIME -> {
                    LocalTime.parse(text)
                    true
                }
                MONEY -> BigDecimal(text).let { it.signum() >= 0 && it.scale() <= 2 }
                BOOLEAN -> text.equals("true", ignoreCase = true) || text.equals("false", ignoreCase = true)
            }
        }.getOrDefault(false)
    }
}
