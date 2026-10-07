package com.performily.flowboard.features.payroll.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET

/**
 * Lectura de Workspace que necesita Payroll (ACL). Usa sus propios DTOs para que un cambio
 * en el modelo de Workspace no afecte a Payroll.
 */
interface PayrollWorkspaceService {

    @GET("employees")
    suspend fun getEmployees(): Response<List<PayrollEmployeeDto>>

    @GET("areas")
    suspend fun getAreas(): Response<List<PayrollAreaDto>>
}
