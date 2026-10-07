package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.repository.BenefitTypeRepository
import javax.inject.Inject

/** MA-60 · Activa o desactiva un tipo. Uno inactivo sigue en el catálogo pero ya no se puede asignar. */
class ChangeBenefitTypeStatusUseCase @Inject constructor(private val repository: BenefitTypeRepository) {
    suspend operator fun invoke(benefitType: BenefitType): Result<BenefitType> =
        if (benefitType.active) repository.deactivate(benefitType.id) else repository.activate(benefitType.id)
}
