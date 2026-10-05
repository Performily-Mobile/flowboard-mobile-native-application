package com.performily.flowboard.features.workspace.domain.valueobject

import java.time.LocalDate

data class TerminationDetails(
    val reason: String,
    val terminationDate: LocalDate
) {
    init {
        require(reason.isNotBlank()) { "El motivo de cese es obligatorio." }
        require(reason.length <= 500) { "El motivo admite hasta 500 caracteres." }
    }
}
