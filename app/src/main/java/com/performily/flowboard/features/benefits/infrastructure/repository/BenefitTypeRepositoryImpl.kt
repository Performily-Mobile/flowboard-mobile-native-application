package com.performily.flowboard.features.benefits.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.repository.BenefitTypeRepository
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.infrastructure.mapper.BenefitsMapper
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitTypeService
import javax.inject.Inject

class BenefitTypeRepositoryImpl @Inject constructor(
    private val service: BenefitTypeService
) : BenefitTypeRepository {

    override suspend fun getBenefitTypes(activeOnly: Boolean): Result<List<BenefitType>> =
        apiCall { service.getBenefitTypes(activeOnly) }
            .mapCatching { dtos -> dtos.map { BenefitsMapper.toDomain(it) } }

    override suspend fun createBenefitType(
        name: String,
        description: String?,
        unit: BenefitUnit,
        hasBalance: Boolean
    ): Result<BenefitType> =
        apiCall { service.createBenefitType(BenefitsMapper.toCreateRequest(name, description, unit, hasBalance)) }
            .mapCatching { BenefitsMapper.toDomain(it) }

    override suspend fun activate(benefitTypeId: Long): Result<BenefitType> =
        apiCall { service.activate(benefitTypeId) }.mapCatching { BenefitsMapper.toDomain(it) }

    override suspend fun deactivate(benefitTypeId: Long): Result<BenefitType> =
        apiCall { service.deactivate(benefitTypeId) }.mapCatching { BenefitsMapper.toDomain(it) }
}
