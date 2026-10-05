package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.repository.AreaRepository
import javax.inject.Inject

class GetAreasUseCase @Inject constructor(private val repository: AreaRepository) {

    suspend operator fun invoke(onlyActive: Boolean = false): Result<List<Area>> {
        return repository.getAreas()
            .map { areas -> areas.filter { !onlyActive || it.active }.sortedBy { it.name.lowercase() } }
    }
}
