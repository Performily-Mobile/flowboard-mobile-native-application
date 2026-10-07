package com.performily.flowboard.features.request.domain.valueobject

/**
 * Saldo que se descuenta cuando se aprueba una solicitud del tipo.
 *
 * @property label opción del botón segmentado "Descuenta saldo" (MA-55)
 * @property catalogLabel detalle en la lista de tipos (MA-54)
 */
enum class BalanceDeduction(val label: String, val catalogLabel: String) {
    NONE("Ninguno", "Sin descuento de saldo"),
    VACATION_DAYS("Vacaciones", "Descuenta días de vacaciones"),
    BENEFIT_BALANCE("Beneficio", "Descuenta saldo de beneficio")
}
