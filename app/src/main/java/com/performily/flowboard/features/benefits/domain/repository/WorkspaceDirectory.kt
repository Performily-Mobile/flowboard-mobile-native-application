package com.performily.flowboard.features.benefits.domain.repository

import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption

/**
 * Lo que Benefits necesita de Workspace para asignar: áreas y colaboradores activos.
 * Es la capa anticorrupción: Benefits no usa directamente las entidades de Workspace.
 */
interface WorkspaceDirectory {
    suspend fun getActiveAreas(): Result<List<AreaOption>>
    suspend fun getActiveEmployees(): Result<List<EmployeeOption>>
}
