package com.performily.flowboard.core.domain

@JvmInline
value class EmployeeId(val value: Long) {
    init {
        require(value > 0) { "El identificador del colaborador debe ser positivo." }
    }
}
