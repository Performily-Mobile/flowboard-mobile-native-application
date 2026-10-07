package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.repository.BenefitTypeRepository
import javax.inject.Inject

/** MA-60 · Catálogo de beneficios, ordenado por nombre. activeOnly=true para elegir al asignar. */
class GetBenefitTypesUseCase @Inject constructor(private val repository: BenefitTypeRepository) {
    suspend operator fun invoke(activeOnly: Boolean = false): Result<List<BenefitType>> =
        repository.getBenefitTypes(activeOnly).map { types -> types.sortedBy { it.name.lowercase() } }
}
