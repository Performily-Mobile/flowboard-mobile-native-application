package com.performily.flowboard.features.workspace.domain.valueobject

data class IdentityDocument(
    val type: IdentityDocumentType,
    val number: String
) {
    init {
        val valid = when (type) {
            IdentityDocumentType.DNI -> Regex("^\\d{8}$").matches(number)
            IdentityDocumentType.CE -> Regex("^[A-Za-z0-9]{9,12}$").matches(number)
            IdentityDocumentType.PASSPORT -> Regex("^[A-Za-z0-9]{6,12}$").matches(number)
        }
        require(valid) {
            when (type) {
                IdentityDocumentType.DNI -> "El DNI debe tener exactamente 8 dígitos."
                IdentityDocumentType.CE -> "El carné de extranjería debe tener de 9 a 12 caracteres alfanuméricos."
                IdentityDocumentType.PASSPORT -> "El pasaporte debe tener de 6 a 12 caracteres alfanuméricos."
            }
        }
    }
}
