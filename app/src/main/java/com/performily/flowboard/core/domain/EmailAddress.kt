package com.performily.flowboard.core.domain

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Este campo es obligatorio." }
        require(EMAIL_PATTERN.matches(value)) { "Ingresa un correo con formato válido." }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
