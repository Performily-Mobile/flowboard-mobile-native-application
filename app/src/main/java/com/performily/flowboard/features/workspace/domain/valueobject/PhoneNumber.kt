package com.performily.flowboard.features.workspace.domain.valueobject

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Este campo es obligatorio." }
        require(PHONE_PATTERN.matches(value)) { "Ingresa un teléfono de 7 a 15 dígitos." }
    }

    companion object {
        private val PHONE_PATTERN = Regex("^\\+?\\d{7,15}$")

        fun of(raw: String): PhoneNumber = PhoneNumber(raw.replace(" ", "").replace("-", ""))
    }
}
