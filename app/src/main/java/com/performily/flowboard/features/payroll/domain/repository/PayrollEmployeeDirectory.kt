package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee

/**
 * Anti-corruption layer hacia Workspace: Payroll solo necesita la identidad del colaborador
 * (nombre y área) y la toma tal como la define Workspace.
 */
interface PayrollEmployeeDirectory {

    suspend fun getEmployees(): Result<List<PayrollEmployee>>

    suspend fun getAreas(): Result<List<PayrollArea>>
}
