package com.performily.flowboard.features.payroll.domain.entity

import com.performily.flowboard.core.domain.EmployeeId

/**
 * Colaborador tal como lo necesita Payroll (dueño de la boleta). Viene de Workspace por el ACL;
 * Payroll no modifica nada de él.
 */
data class PayrollEmployee(
    val id: EmployeeId,
    val firstName: String,
    val lastName: String,
    val areaId: Long?
) {
    /** "Pedro Huamán Quispe" */
    val fullName: String get() = "$firstName $lastName".trim()

    /** "Huamán Quispe, Pedro" */
    val sortableName: String
        get() = if (lastName.isBlank()) firstName else "$lastName, $firstName"

    /** "PH" */
    val initials: String
        get() = listOfNotNull(firstName.trim().firstOrNull(), lastName.trim().firstOrNull())
            .joinToString("").uppercase()
}
