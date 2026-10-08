package com.performily.flowboard.features.request.domain.valueobject

enum class BalanceDeduction(val label: String, val catalogLabel: String) {
    NONE("Ninguno", "Sin descuento de saldo"),
    VACATION_DAYS("Vacaciones", "Descuenta días de vacaciones"),
    BENEFIT_BALANCE("Beneficio", "Descuenta saldo de beneficio")
}
