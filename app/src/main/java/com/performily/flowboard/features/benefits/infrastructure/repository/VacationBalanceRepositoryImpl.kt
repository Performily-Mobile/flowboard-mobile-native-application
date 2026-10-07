package com.performily.flowboard.features.benefits.infrastructure.repository

import com.performily.flowboard.core.network.ApiException
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance
import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.repository.VacationBalanceRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import com.performily.flowboard.features.benefits.infrastructure.local.VacationBalanceCache
import com.performily.flowboard.features.benefits.infrastructure.mapper.VacationBalanceMapper
import com.performily.flowboard.features.benefits.infrastructure.remote.VacationBalanceService
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

class VacationBalanceRepositoryImpl @Inject constructor(
    private val service: VacationBalanceService,
    private val cache: VacationBalanceCache
) : VacationBalanceRepository {

    override suspend fun getMyBalance(employeeId: Long): Result<SyncedVacationBalance> {
        val response = apiCall { service.getMyBalance(employeeId) }
        response.onSuccess { dto ->
            val now = LocalDateTime.now()
            cache.save(employeeId, dto, now)
            return runCatching { SyncedVacationBalance(VacationBalanceMapper.toDomain(dto), now, fromCache = false) }
        }
        val error = response.exceptionOrNull() ?: IllegalStateException("No se pudo cargar el saldo.")
        // Sin conexión (o el servidor no responde) se muestra la última copia guardada.
        val cached = if (error.isConnectionProblem()) cache.load(employeeId) else null
        return if (cached != null) {
            runCatching { SyncedVacationBalance(VacationBalanceMapper.toDomain(cached.balance), cached.syncedAt, fromCache = true) }
        } else {
            Result.failure(error)
        }
    }

    override suspend fun getBalances(areaId: Long?): Result<List<VacationBalance>> =
        apiCall { service.getBalances(areaId) }
            .mapCatching { dtos -> dtos.map { VacationBalanceMapper.toDomain(it) } }

    override suspend fun getBalance(employeeId: Long): Result<VacationBalance> =
        apiCall { service.getBalance(employeeId) }.mapCatching { VacationBalanceMapper.toDomain(it) }

    override suspend fun adjust(
        employeeId: Long,
        operation: AdjustmentOperation,
        days: BigDecimal,
        reason: String,
        authorId: Long
    ): Result<VacationBalance> {
        val request = VacationBalanceMapper.toAdjustRequest(operation, days, reason, authorId)
        return apiCall { service.adjust(employeeId, request) }.mapCatching { VacationBalanceMapper.toDomain(it) }
    }

    /** Los errores del backend traen código; los de red o de un servidor caído no. */
    private fun Throwable.isConnectionProblem(): Boolean = this is ApiException && code == null
}
