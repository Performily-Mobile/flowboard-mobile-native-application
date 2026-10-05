package com.performily.flowboard.features.workspace.infrastructure.repository

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.repository.PositionRepository
import com.performily.flowboard.features.workspace.infrastructure.mapper.PositionMapper
import com.performily.flowboard.features.workspace.infrastructure.remote.PositionService
import javax.inject.Inject

class PositionRepositoryImpl @Inject constructor(
    private val service: PositionService
) : PositionRepository {

    override suspend fun getPositions(areaId: Long?): Result<List<Position>> {
        return apiCall { service.getPositions(areaId) }
            .mapCatching { dtos -> dtos.map { PositionMapper.toDomain(it) } }
    }

    override suspend fun createPosition(title: String, areaId: Long, referenceSalary: Money): Result<Position> {
        val request = PositionMapper.toCreateRequest(title, areaId, referenceSalary)
        return apiCall { service.createPosition(request) }
            .mapCatching { dto -> PositionMapper.toDomain(dto) }
    }
}
