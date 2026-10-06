package com.performily.flowboard.features.workspace.domain.repository

import com.performily.flowboard.features.workspace.domain.entity.Area

interface AreaRepository {

    suspend fun getAreas(): Result<List<Area>>

    suspend fun createArea(name: String, description: String?): Result<Area>

    suspend fun deactivateArea(id: Long): Result<Area>
}
