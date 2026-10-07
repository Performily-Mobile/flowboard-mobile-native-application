package com.performily.flowboard.features.request.domain.valueobject

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tipo de dato de un campo del formulario. El valor siempre viaja como texto:
 * DATE en yyyy-MM-dd, TIME en HH:mm y BOOLEAN como true o false (igual que el backend).
 *
 * @property label nombre que se muestra al configurar el campo (MA-55)
 */
enum class FieldDataType(val label: String) {
    TEXT("Texto"),
    NUMBER("Número"),
    DATE("Fecha"),
    TIME("Hora"),
    MONEY("Monto"),
    BOOLEAN("Sí / No");

    /** Revisa que el texto tenga el formato que espera el backend. */
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
