package com.performily.flowboard.features.request.infrastructure.repository

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestFieldValue
import com.performily.flowboard.features.request.domain.repository.RequestRepository
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import com.performily.flowboard.features.request.infrastructure.mapper.RequestMapper
import com.performily.flowboard.features.request.infrastructure.remote.RequestService
import javax.inject.Inject

class RequestRepositoryImpl @Inject constructor(
    private val service: RequestService
) : RequestRepository {

    override suspend fun getRequestsOf(requesterId: Long, status: RequestStatus?): Result<List<Request>> =
        apiCall { service.getMyRequests(requesterId, status?.name) }
            .mapCatching { dtos -> dtos.map { RequestMapper.toDomain(it) } }

    override suspend fun getPendingForApprover(approverId: Long, requestTypeId: Long?): Result<List<Request>> =
        apiCall { service.getPendingForApprover(approverId, requestTypeId) }
            .mapCatching { dtos -> dtos.map { RequestMapper.toDomain(it) } }

    override suspend fun getPendingForHrStaff(requestTypeId: Long?): Result<List<Request>> =
        apiCall { service.getPendingForHrStaff(requestTypeId) }
            .mapCatching { dtos -> dtos.map { RequestMapper.toDomain(it) } }

    override suspend fun getRequest(requestId: Long): Result<Request> =
        apiCall { service.getRequest(requestId) }.mapCatching { RequestMapper.toDomain(it) }

    override suspend fun submit(
        requesterId: Long,
        requestTypeId: Long,
        period: RequestPeriod?,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>
    ): Result<Request> {
        val request = RequestMapper.toSubmitRequest(requesterId, requestTypeId, period, fieldValues, attachments)
        return apiCall { service.submit(request) }.mapCatching { RequestMapper.toDomain(it) }
    }

    override suspend fun approve(requestId: Long, actorId: Long): Result<Request> =
        apiCall { service.approve(requestId, RequestMapper.toResolveRequest(actorId)) }
            .mapCatching { RequestMapper.toDomain(it) }

    override suspend fun reject(requestId: Long, actorId: Long, reason: String): Result<Request> =
        apiCall { service.reject(requestId, RequestMapper.toResolveRequest(actorId, reason)) }
            .mapCatching { RequestMapper.toDomain(it) }

    override suspend fun returnForReview(requestId: Long, actorId: Long, comment: String): Result<Request> =
        apiCall { service.returnForReview(requestId, RequestMapper.toResolveRequest(actorId, comment)) }
            .mapCatching { RequestMapper.toDomain(it) }

    override suspend fun resubmit(
        requestId: Long,
        actorId: Long,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>,
        comment: String?
    ): Result<Request> {
        val request = RequestMapper.toResubmitRequest(actorId, fieldValues, attachments, comment)
        return apiCall { service.resubmit(requestId, request) }.mapCatching { RequestMapper.toDomain(it) }
    }

    override suspend fun cancel(requestId: Long, actorId: Long): Result<Request> =
        apiCall { service.cancel(requestId, RequestMapper.toResolveRequest(actorId)) }
            .mapCatching { RequestMapper.toDomain(it) }
}
