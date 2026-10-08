package com.performily.flowboard.features.wellbeing.domain.valueobject

/**
 * Ubicación de un espacio de trabajo.
 *
 * @property reference referencia opcional (por ejemplo "Nave 2")
 */
data class OfficeLocation(
    val address: String,
    val floor: String,
    val reference: String?
) {
    /** "Sede Callao · Piso 3", como se muestra debajo del nombre del espacio. */
    val summary: String
        get() = "$address · Piso $floor"
}
