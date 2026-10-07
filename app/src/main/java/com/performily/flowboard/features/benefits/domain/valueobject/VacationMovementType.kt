package com.performily.flowboard.features.benefits.domain.valueobject

/**
 * Motivo de un cambio en el saldo de vacaciones.
 * ACCRUAL: días ganados (acumulación inicial y mensual).
 * USAGE: días tomados por una solicitud aprobada.
 * REVERSAL: días devueltos al anular una solicitud aprobada.
 * MANUAL_ADJUSTMENT: corrección hecha por RR.HH. con motivo y autor (US42).
 */
enum class VacationMovementType {
    ACCRUAL,
    USAGE,
    REVERSAL,
    MANUAL_ADJUSTMENT
}
