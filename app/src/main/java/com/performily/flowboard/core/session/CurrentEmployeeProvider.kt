package com.performily.flowboard.core.session

import com.performily.flowboard.core.domain.EmployeeId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Indica qué colaborador usa la app.
 * TEMPORAL: mientras no exista el bounded context IAM (login), devuelve un id fijo.
 * Cuando IAM esté listo, este valor se toma de la sesión iniciada.
 */
@Singleton
class CurrentEmployeeProvider @Inject constructor() {

    fun currentEmployeeId(): EmployeeId = EmployeeId(DEFAULT_EMPLOYEE_ID)

    private companion object {
        const val DEFAULT_EMPLOYEE_ID = 1L
    }
}
