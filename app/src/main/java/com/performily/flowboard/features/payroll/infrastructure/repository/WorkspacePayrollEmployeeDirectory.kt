package com.performily.flowboard.features.payroll.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee
import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.infrastructure.mapper.PayrollEmployeeMapper
import com.performily.flowboard.features.payroll.infrastructure.remote.PayrollWorkspaceService
import javax.inject.Inject

/** ACL hacia Workspace: lee colaboradores y áreas con DTOs propios de Payroll. */
class WorkspacePayrollEmployeeDirectory @Inject constructor(
    private val service: PayrollWorkspaceService
) : PayrollEmployeeDirectory {

    override suspend fun getEmployees(): Result<List<PayrollEmployee>> =
        apiCall { service.getEmployees() }
            .mapCatching { dtos -> dtos.map { PayrollEmployeeMapper.toDomain(it) } }

    override suspend fun getAreas(): Result<List<PayrollArea>> =
        apiCall { service.getAreas() }
            .mapCatching { dtos -> dtos.map { PayrollEmployeeMapper.toDomain(it) } }
}
