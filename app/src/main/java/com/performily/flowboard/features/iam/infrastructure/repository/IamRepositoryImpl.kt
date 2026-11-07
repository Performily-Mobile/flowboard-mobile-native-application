package com.performily.flowboard.features.iam.infrastructure.repository

import com.performily.flowboard.features.iam.domain.entity.AuthSession
import com.performily.flowboard.features.iam.domain.entity.Account
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import com.performily.flowboard.features.iam.infrastructure.local.SecureSessionStore
import com.performily.flowboard.features.iam.infrastructure.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IamRepositoryImpl @Inject constructor(private val api: IamService, private val secure: SecureSessionStore): IamRepository {
    private val current = MutableStateFlow(secure.read())
    override val session: StateFlow<AuthSession?> = current
    override suspend fun signIn(username: String, password: String): Result<AuthSession> = runCatching {
        val response = api.login(LoginRequest(username, password))
        if (!response.isSuccessful) throw IllegalStateException(when (response.code()) {
            401 -> "El usuario o la contraseña no son correctos."
            403, 422 -> "Tu cuenta está inhabilitada. Comunícate con Recursos Humanos."
            else -> "No se pudo iniciar sesión (HTTP ${response.code()})."
        })
        val body = response.body() ?: error("Respuesta de inicio de sesión vacía")
        AuthSession(body.token, body.role, body.employeeId, body.mustChangePassword, username).also { secure.save(it); current.value = it }
    }
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response = api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        if (!response.isSuccessful) error("No se pudo cambiar la contraseña (HTTP ${response.code()}).")
        // Never keep the old JWT after a password change if the backend doesn't issue a replacement.
        val newToken = response.body()?.token
        if (!newToken.isNullOrBlank()) {
            val old = current.value ?: error("La sesión expiró")
            val updated = old.copy(token = newToken, mustChangePassword = false)
            secure.save(updated); current.value = updated
        } else { secure.clear(); current.value = null }
    }
    override suspend fun signOut(): Result<Unit> {
        return try { val response = api.logout(); if (!response.isSuccessful) error("No se pudo revocar el token (${response.code()})."); Result.success(Unit) }
        catch (e: Exception) { Result.failure(e) }
        finally { secure.clear(); current.value = null }
    }
    override suspend fun accounts() = runCatching {
        val response = api.accounts(); if (!response.isSuccessful) error("Error HTTP ${response.code()}")
        response.body().orEmpty().map { Account(it.id, it.employeeId, it.username, it.role, it.status) }
    }
    override suspend fun changeRole(id: Long, role: String) = runCatching {
        val r = api.changeRole(id, RoleRequest(role)); if (!r.isSuccessful) error("No se pudo cambiar el rol (HTTP ${r.code()}).")
    }
    override suspend fun resetPassword(id: Long) = runCatching {
        val r = api.resetPassword(id); if (!r.isSuccessful) error("No se pudo restablecer la contraseña (HTTP ${r.code()}).")
        r.body()?.temporaryPassword?.takeIf { it.isNotBlank() } ?: error("El servidor no devolvió una contraseña temporal")
    }
    override fun setBiometricEnabled(enabled: Boolean) = secure.setBiometricEnabled(enabled)
    override fun isBiometricEnabled() = secure.biometricEnabled()
}
