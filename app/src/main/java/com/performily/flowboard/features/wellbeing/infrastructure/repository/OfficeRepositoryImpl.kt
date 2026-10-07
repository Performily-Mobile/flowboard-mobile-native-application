package com.performily.flowboard.features.wellbeing.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.wellbeing.domain.entity.Office
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus
import com.performily.flowboard.features.wellbeing.domain.repository.OfficeRepository
import com.performily.flowboard.features.wellbeing.infrastructure.mapper.OfficeMapper
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeService
import javax.inject.Inject

class OfficeRepositoryImpl @Inject constructor(
    private val service: OfficeService
) : OfficeRepository {

    override suspend fun getOffices(): Result<List<OfficeStatus>> {
        return apiCall { service.getOffices() }
            .mapCatching { dtos -> dtos.map { OfficeMapper.toDomain(it) } }
    }

    override suspend fun getOfficeStatus(officeId: Long): Result<OfficeStatus> {
        return apiCall { service.getOfficeStatus(officeId) }
            .mapCatching { dto -> OfficeMapper.toDomain(dto) }
    }

    override suspend fun createOffice(
        name: String,
        area: String?,
        address: String,
        floor: String,
        reference: String?
    ): Result<Office> {
        val request = OfficeMapper.toCreateRequest(name, area, address, floor, reference)
        return apiCall { service.createOffice(request) }
            .mapCatching { dto -> OfficeMapper.toDomain(dto) }
    }
}
