package com.performily.flowboard.features.benefits.domain.valueobject

/** Sentido de un ajuste manual de vacaciones (MA-63): agregar o descontar días. */
enum class AdjustmentOperation(val label: String) {
    ADD("Agregar días"),
    DEDUCT("Descontar días")
}
