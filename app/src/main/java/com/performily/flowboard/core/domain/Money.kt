package com.performily.flowboard.core.domain

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: String = DEFAULT_CURRENCY
) {
    init {
        require(amount >= BigDecimal.ZERO) { "El monto no puede ser negativo." }
        require(currency.length == 3) { "La moneda debe tener 3 letras." }
    }

    companion object {
        const val DEFAULT_CURRENCY = "PEN"
    }
}
