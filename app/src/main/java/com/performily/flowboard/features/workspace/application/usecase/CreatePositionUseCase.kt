package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.repository.PositionRepository
import javax.inject.Inject

class CreatePositionUseCase @Inject constructor(private val repository: PositionRepository) {

    suspend operator fun invoke(title: String, areaId: Long, referenceSalary: Money): Result<Position> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre de la posición es obligatorio."))
        }
        if (trimmedTitle.length > 80) {
            return Result.failure(IllegalArgumentException("El nombre admite hasta 80 caracteres."))
        }
        return repository.createPosition(trimmedTitle, areaId, referenceSalary)
    }
}
