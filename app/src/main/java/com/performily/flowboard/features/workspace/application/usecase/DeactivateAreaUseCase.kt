package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.repository.AreaRepository
import javax.inject.Inject

class DeactivateAreaUseCase @Inject constructor(private val repository: AreaRepository) {

    suspend operator fun invoke(area: Area): Result<Area> {
        if (!area.active) {
            return Result.failure(IllegalStateException("El área ya está desactivada."))
        }
        if (!area.canBeDeactivated) {
            return Result.failure(
                IllegalStateException("El área tiene colaboradores activos. Reasígnalos antes de desactivarla.")
            )
        }
        return repository.deactivateArea(area.id)
    }
}
