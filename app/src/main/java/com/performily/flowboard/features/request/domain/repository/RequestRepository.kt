package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestFieldValue
import com.performily.flowboard.features.request.domain.entity.RequestPage
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.domain.valueobject.RequestSortOrder
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus

interface RequestRepository {
    suspend fun getAll(
        statuses: List<RequestStatus> = emptyList(),
        approverId: Long? = null,
        page: Int = 0,
        size: Int = RequestPage.DEFAULT_SIZE
    ): Result<RequestPage>
    suspend fun getRequestsOf(
        requesterId: Long,
        status: RequestStatus?,
        page: Int = 0,
        size: Int = RequestPage.DEFAULT_SIZE
    ): Result<RequestPage>
    suspend fun getPendingForApprover(
        approverId: Long,
        requestTypeId: Long?,
        sort: RequestSortOrder = RequestSortOrder.OLDEST_FIRST,
        page: Int = 0,
        size: Int = RequestPage.DEFAULT_SIZE
    ): Result<RequestPage>
    suspend fun getPendingForHrStaff(
        requestTypeId: Long?,
        sort: RequestSortOrder = RequestSortOrder.OLDEST_FIRST,
        page: Int = 0,
        size: Int = RequestPage.DEFAULT_SIZE
    ): Result<RequestPage>
    suspend fun getRequest(requestId: Long): Result<Request>
    suspend fun submit(
        requesterId: Long,
        requestTypeId: Long,
        period: RequestPeriod?,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>
    ): Result<Request>
    suspend fun approve(requestId: Long, actorId: Long): Result<Request>
    suspend fun reject(requestId: Long, actorId: Long, reason: String): Result<Request>
    suspend fun returnForReview(requestId: Long, actorId: Long, comment: String): Result<Request>
    suspend fun resubmit(
        requestId: Long,
        actorId: Long,
        fieldValues: List<RequestFieldValue>,
        attachments: List<FileReference>,
        comment: String?
    ): Result<Request>
    suspend fun cancel(requestId: Long, actorId: Long): Result<Request>
}
