package com.performily.flowboard.features.workspace.infrastructure.repository

import com.performily.flowboard.core.network.ResponseCache
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.repository.AreaRepository
import com.performily.flowboard.features.workspace.infrastructure.mapper.AreaMapper
import com.performily.flowboard.features.workspace.infrastructure.remote.AreaService
import javax.inject.Inject

class AreaRepositoryImpl @Inject constructor(
    private val service: AreaService,
    private val cache: ResponseCache
) : AreaRepository {

    override suspend fun getAreas(): Result<List<Area>> {
        return cache.getOrLoad("areas") {
            apiCall { service.getAreas() }
                .mapCatching { dtos -> dtos.map { AreaMapper.toDomain(it) } }
        }
    }

    override suspend fun createArea(name: String, description: String?): Result<Area> {
        val request = AreaMapper.toCreateRequest(name, description)
        return apiCall { service.createArea(request) }
            .mapCatching { dto -> AreaMapper.toDomain(dto) }
            .onSuccess { cache.invalidateAll() }
    }

    override suspend fun deactivateArea(id: Long): Result<Area> {
        return apiCall { service.deactivateArea(id) }
            .mapCatching { dto -> AreaMapper.toDomain(dto) }
            .onSuccess { cache.invalidateAll() }
    }
}
