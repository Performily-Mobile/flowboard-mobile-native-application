package com.performily.flowboard.features.request.domain.repository

import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction

interface RequestTypeRepository {
    suspend fun getRequestTypes(activeOnly: Boolean): Result<List<RequestType>>
    suspend fun createRequestType(
        name: String,
        description: String?,
        requiresAttachment: Boolean,
        balanceDeduction: BalanceDeduction,
        fields: List<NewRequestField>
    ): Result<RequestType>
    suspend fun activate(requestTypeId: Long): Result<RequestType>
    suspend fun deactivate(requestTypeId: Long): Result<RequestType>
    suspend fun delete(requestTypeId: Long): Result<Unit>
}
