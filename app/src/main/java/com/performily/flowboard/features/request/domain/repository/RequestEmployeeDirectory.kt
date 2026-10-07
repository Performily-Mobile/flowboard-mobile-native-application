package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.entity.RequestPerson

/**
 * Lo que Request necesita de Workspace: nombres, puestos, jefe directo y el equipo
 * de un jefe. Es la capa anticorrupción: Request no usa las entidades de Workspace.
 */
interface RequestEmployeeDirectory {
    suspend fun getPerson(employeeId: Long): Result<RequestPerson>
    suspend fun getPeople(): Result<List<RequestPerson>>
    suspend fun getTeam(managerId: Long): Result<List<RequestPerson>>
}
