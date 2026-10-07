package com.performily.flowboard.features.benefits.domain.repository

import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit

interface BenefitTypeRepository {
    suspend fun getBenefitTypes(activeOnly: Boolean): Result<List<BenefitType>>
    suspend fun createBenefitType(name: String, description: String?, unit: BenefitUnit, hasBalance: Boolean): Result<BenefitType>
    suspend fun activate(benefitTypeId: Long): Result<BenefitType>
    suspend fun deactivate(benefitTypeId: Long): Result<BenefitType>
}
