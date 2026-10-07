package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.repository.BenefitTypeRepository
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import javax.inject.Inject

/**
 * Creates a benefit type (MA-60, US37).
 *
 * The name is required and must be unique within the catalog.
 */
class CreateBenefitTypeUseCase @Inject constructor(private val repository: BenefitTypeRepository) {
    suspend operator fun invoke(name: String, unit: BenefitUnit, hasBalance: Boolean, description: String? = null): Result<BenefitType> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return Result.failure(IllegalArgumentException("Ingresa el nombre del beneficio."))
        if (cleanName.length > MAX_NAME) return Result.failure(IllegalArgumentException("El nombre admite hasta $MAX_NAME caracteres."))
        return repository.createBenefitType(cleanName, description?.trim()?.takeIf { it.isNotEmpty() }, unit, hasBalance)
    }

    private companion object {
        const val MAX_NAME = 80
    }
}
