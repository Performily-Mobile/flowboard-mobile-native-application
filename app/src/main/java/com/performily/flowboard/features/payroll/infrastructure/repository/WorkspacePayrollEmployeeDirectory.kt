package com.performily.flowboard.features.payroll.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee
import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.infrastructure.mapper.PayrollEmployeeMapper
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollWorkspaceService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anti-corruption layer towards Workspace: reads employees and areas with Payroll's own DTOs.
 *
 * The list of employees is cached for a short time so it is not requested again on every filter
 * change or reload.
 */
@Singleton
class WorkspacePayrollEmployeeDirectory @Inject constructor(
    private val service: PayrollWorkspaceService
) : PayrollEmployeeDirectory {

    @Volatile
    private var cachedEmployees: List<PayrollEmployee>? = null

    @Volatile
    private var cachedAt: Long = 0L

    /**
     * Returns the employees, from the cache when it is recent enough.
     *
     * @return the employees as Payroll needs them, or a failure when Workspace cannot be reached
     */
    override suspend fun getEmployees(): Result<List<PayrollEmployee>> {
        val now = System.currentTimeMillis()
        val cached = cachedEmployees
        if (cached != null && now - cachedAt < CACHE_MILLIS) return Result.success(cached)
        return apiCall { service.getEmployees() }
            .mapCatching { dtos -> dtos.map { PayrollEmployeeMapper.toDomain(it) } }
            .onSuccess { employees ->
                cachedEmployees = employees
                cachedAt = now
            }
    }

    override suspend fun getAreas(): Result<List<PayrollArea>> =
        apiCall { service.getAreas() }
            .mapCatching { dtos -> dtos.map { PayrollEmployeeMapper.toDomain(it) } }

    private companion object {
        const val CACHE_MILLIS = 60_000L
    }
}
