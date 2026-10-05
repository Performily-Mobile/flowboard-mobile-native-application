package com.performily.flowboard.features.workspace.domain.valueobject

data class PersonName(
    val firstName: String,
    val lastName: String
) {
    init {
        require(firstName.isNotBlank()) { "Los nombres son obligatorios." }
        require(lastName.isNotBlank()) { "Los apellidos son obligatorios." }
        require(firstName.length <= 50) { "Los nombres admiten hasta 50 caracteres." }
        require(lastName.length <= 80) { "Los apellidos admiten hasta 80 caracteres." }
        require(NAME_PATTERN.matches(firstName)) { "Los nombres solo admiten letras, espacios, apóstrofos y guiones." }
        require(NAME_PATTERN.matches(lastName)) { "Los apellidos solo admiten letras, espacios, apóstrofos y guiones." }
    }

    val fullName: String get() = "$firstName $lastName"

    val sortableName: String get() = "$lastName, $firstName"

    val initials: String
        get() = "${firstName.trim().first()}${lastName.trim().first()}".uppercase()

    private companion object {
        val NAME_PATTERN = Regex("^[\\p{L} '\\-]+$")
    }
}
