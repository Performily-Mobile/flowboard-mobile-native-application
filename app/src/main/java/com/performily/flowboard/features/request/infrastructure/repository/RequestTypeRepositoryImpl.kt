package com.performily.flowboard.features.request.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.repository.RequestTypeRepository
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.infrastructure.mapper.RequestMapper
import com.performily.flowboard.features.request.infrastructure.remote.RequestTypeService
import javax.inject.Inject

class RequestTypeRepositoryImpl @Inject constructor(
    private val service: RequestTypeService
) : RequestTypeRepository {

    override suspend fun getRequestTypes(activeOnly: Boolean): Result<List<RequestType>> =
        apiCall { service.getRequestTypes(activeOnly) }
            .mapCatching { dtos -> dtos.map { RequestMapper.toDomain(it) } }

    override suspend fun createRequestType(
        name: String,
        description: String?,
        requiresAttachment: Boolean,
        balanceDeduction: BalanceDeduction,
        fields: List<NewRequestField>
    ): Result<RequestType> {
        val request = RequestMapper.toCreateRequest(name, description, requiresAttachment, balanceDeduction, fields)
        return apiCall { service.createRequestType(request) }.mapCatching { RequestMapper.toDomain(it) }
    }

    override suspend fun activate(requestTypeId: Long): Result<RequestType> =
        apiCall { service.activate(requestTypeId) }.mapCatching { RequestMapper.toDomain(it) }

    override suspend fun deactivate(requestTypeId: Long): Result<RequestType> =
        apiCall { service.deactivate(requestTypeId) }.mapCatching { RequestMapper.toDomain(it) }

    override suspend fun delete(requestTypeId: Long): Result<Unit> =
        apiCall { service.delete(requestTypeId) }.map { }
}
