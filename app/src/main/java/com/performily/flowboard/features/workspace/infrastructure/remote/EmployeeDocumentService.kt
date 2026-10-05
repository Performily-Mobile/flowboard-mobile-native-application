package com.performily.flowboard.features.workspace.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EmployeeDocumentService {

    @GET("employees/{employeeId}/documents")
    suspend fun getDocuments(@Path("employeeId") employeeId: Long): Response<List<EmployeeDocumentDto>>
}
