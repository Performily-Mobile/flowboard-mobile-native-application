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

    /**
     * Loads the balance of the signed-in employee.
     *
     * The local copy is saved only when the response could be mapped correctly. When there is no
     * connection (or the server does not answer) the last saved copy is returned instead.
     *
     * @param employeeId the employee whose balance is requested.
     * @return the balance with its sync timestamp, or the original failure when no cached copy applies.
     */
    override suspend fun getMyBalance(employeeId: Long): Result<SyncedVacationBalance> {
        val response = apiCall { service.getMyBalance(employeeId) }
        response.onSuccess { dto ->
            val now = LocalDateTime.now()
            return runCatching { VacationBalanceMapper.toDomain(dto) }.map { balance ->
                runCatching { cache.save(employeeId, dto, now) }
                SyncedVacationBalance(balance, now, fromCache = false)
            }
        }
        val error = response.exceptionOrNull() ?: IllegalStateException("No se pudo cargar el saldo.")
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

    /**
     * Tells whether this failure is a connectivity problem.
     *
     * Backend errors carry a code; network errors do not. A response that does not come from the
     * backend (such as "Error 502" from a proxy) has no code either, but the server did answer,
     * so it is not treated as a lack of connection.
     */
    private fun Throwable.isConnectionProblem(): Boolean {
        val api = this as? ApiException ?: return false
        return api.code == null && !HTTP_ERROR_MESSAGE.matches(api.message)
    }

    private companion object {
        val HTTP_ERROR_MESSAGE = Regex("""^Error \d+$""")
    }
}
