package com.performily.flowboard.core.session

import com.performily.flowboard.core.domain.EmployeeId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Indica qué colaborador usa la app y con qué rol.
 * TEMPORAL: mientras no exista el bounded context IAM (login), devuelve valores fijos.
 * Cuando IAM esté listo, estos valores se toman de la sesión iniciada.
 * Para probar la vista del colaborador, cambia DEFAULT_ROLE a UserRole.EMPLOYEE.
 */
@Singleton
class CurrentEmployeeProvider @Inject constructor() {

    fun currentEmployeeId(): EmployeeId = EmployeeId(DEFAULT_EMPLOYEE_ID)

    fun currentRole(): UserRole = DEFAULT_ROLE

    private companion object {
        const val DEFAULT_EMPLOYEE_ID = 1L
        val DEFAULT_ROLE = UserRole.HUMAN_RESOURCES
    }
}
