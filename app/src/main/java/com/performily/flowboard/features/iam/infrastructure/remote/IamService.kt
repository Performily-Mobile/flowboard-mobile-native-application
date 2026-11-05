package com.performily.flowboard.features.iam.infrastructure.remote

import retrofit2.Response
import retrofit2.http.*

interface IamService {
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("auth/change-password") suspend fun changePassword(@Body request: ChangePasswordRequest): Response<LoginResponse>
    @POST("auth/logout") suspend fun logout(): Response<Unit>
    @GET("accounts") suspend fun accounts(): Response<List<AccountDto>>
    @PATCH("accounts/{id}/role") suspend fun changeRole(@Path("id") id: Long, @Body request: RoleRequest): Response<Unit>
    @POST("accounts/{id}/reset-password") suspend fun resetPassword(@Path("id") id: Long): Response<Unit>
}
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val role: String, val employeeId: Long, val mustChangePassword: Boolean = false)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class AccountDto(val id: Long, val employeeId: Long, val username: String, val role: String, val status: String)
data class RoleRequest(val role: String)
