package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.repository.PositionRepository
import javax.inject.Inject

class GetPositionsUseCase @Inject constructor(private val repository: PositionRepository) {

    suspend operator fun invoke(areaId: Long? = null, onlyActive: Boolean = false): Result<List<Position>> {
        return repository.getPositions(areaId)
            .map { positions -> positions.filter { !onlyActive || it.active }.sortedBy { it.title.lowercase() } }
    }
}
