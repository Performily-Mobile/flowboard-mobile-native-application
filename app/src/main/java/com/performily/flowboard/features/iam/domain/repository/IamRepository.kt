package com.performily.flowboard.features.iam.domain.repository

import com.performily.flowboard.features.iam.domain.entity.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface IamRepository {
    val session: StateFlow<AuthSession?>
    suspend fun signIn(username: String, password: String): Result<AuthSession>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun accounts(): Result<List<Account>>
    suspend fun changeRole(id: Long, role: String): Result<Unit>
    suspend fun resetPassword(id: Long): Result<Unit>
    fun setBiometricEnabled(enabled: Boolean)
    fun isBiometricEnabled(): Boolean
}
data class Account(val id: Long, val employeeId: Long, val username: String, val role: String, val status: String)
