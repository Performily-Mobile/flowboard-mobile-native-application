package com.performily.flowboard.features.workspace.domain.repository

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.workspace.domain.entity.Position

interface PositionRepository {

    suspend fun getPositions(areaId: Long?): Result<List<Position>>

    suspend fun createPosition(title: String, areaId: Long, referenceSalary: Money): Result<Position>
}
