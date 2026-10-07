package com.performily.flowboard.features.benefits.domain.valueobject

/**
 * Unidad en la que se mide un tipo de beneficio.
 * MONEY en soles, DAYS en días calendario y UNITS en piezas enteras.
 *
 * @property label nombre que se muestra en el catálogo y en el formulario (MA-60)
 * @property quantityLabel etiqueta del campo de cantidad al asignar (MA-61)
 */
enum class BenefitUnit(val label: String, val quantityLabel: String) {
    MONEY("Monto (S/)", "Monto"),
    DAYS("Días", "Cantidad de días"),
    UNITS("Unidades", "Cantidad")
}
