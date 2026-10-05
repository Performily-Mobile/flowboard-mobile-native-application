package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.repository.AreaRepository
import javax.inject.Inject

class CreateAreaUseCase @Inject constructor(private val repository: AreaRepository) {

    suspend operator fun invoke(name: String, description: String?): Result<Area> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre del área es obligatorio."))
        }
        if (trimmedName.length > 80) {
            return Result.failure(IllegalArgumentException("El nombre admite hasta 80 caracteres."))
        }
        return repository.createArea(trimmedName, description?.trim()?.takeIf { it.isNotEmpty() })
    }
}
