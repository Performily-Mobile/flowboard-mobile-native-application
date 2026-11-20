package com.performily.flowboard.features.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.iam.application.usecase.ChangePasswordUseCase
import com.performily.flowboard.features.iam.application.usecase.SignInUseCase
import com.performily.flowboard.features.iam.application.usecase.SignOutUseCase
import com.performily.flowboard.features.iam.domain.entity.Account
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import com.performily.flowboard.features.iam.infrastructure.local.BiometricCredentialStore
import com.performily.flowboard.features.iam.infrastructure.local.BiometricCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.crypto.Cipher
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class IamViewModel @Inject constructor(
    private val repository: IamRepository,
    private val signIn: SignInUseCase,
    private val change: ChangePasswordUseCase,
    private val signOut: SignOutUseCase,
    private val biometricStore: BiometricCredentialStore
): ViewModel() {
    val session = repository.session
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private val _temporaryPassword = MutableStateFlow<String?>(null)
    val temporaryPassword = _temporaryPassword.asStateFlow()
    fun dismissTemporaryPassword() { _temporaryPassword.value = null }
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()
    fun clearFeedback() { _error.value = null; _message.value = null }
    fun login(username: String, password: String) = execute { signIn(username, password).getOrThrow(); Unit }
    fun updatePassword(current: String, next: String, confirm: String) {
        if (next != confirm) { _error.value = "Las contraseñas no coinciden"; return }
        if (!validPassword(next)) { _error.value = "Usa 8 caracteres, mayúscula, minúscula, número y símbolo"; return }
        execute { change(current, next).getOrThrow(); _message.value = "Contraseña actualizada" }
    }
    fun logout() = execute { signOut().getOrThrow() }
    fun loadAccounts() = execute { _accounts.value = repository.accounts().getOrThrow() }
    fun changeRole(id: Long, role: String) = execute { repository.changeRole(id, role).getOrThrow(); _accounts.value = repository.accounts().getOrThrow() }
    fun resetPassword(id: Long) = execute { _temporaryPassword.value = repository.resetPassword(id).getOrThrow(); _message.value = "Contraseña temporal generada. Entrégala por un canal seguro." }
    /** True when the fingerprint sign-in was activated on this device. */
    fun hasBiometricLogin(): Boolean = biometricStore.hasCredentials()

    /** Cipher that the biometric prompt must authorize before the credentials are stored, or null when unavailable. */
    fun biometricEnrollCipher(): Cipher? = biometricStore.encryptCipher()

    /** Cipher that the biometric prompt must authorize before the credentials are read, or null when unavailable. */
    fun biometricLoginCipher(): Cipher? = biometricStore.decryptCipher().also {
        if (it == null && !biometricStore.hasCredentials()) {
            _error.value = "Se registró una huella nueva en el equipo. Ingresa con tu contraseña y vuelve a activar el ingreso con huella."
        }
    }

    /**
     * Signs in with the password and activates the fingerprint sign-in with the same credentials.
     *
     * The credentials are stored before the request so they are saved even if the screen closes as
     * soon as the session starts; a failed sign-in removes them again.
     *
     * @param username corporate e-mail.
     * @param password password.
     * @param cipher encryption cipher authorized by the biometric prompt.
     */
    fun loginAndEnableBiometric(username: String, password: String, cipher: Cipher) = execute {
        biometricStore.save(cipher, BiometricCredentials(username, password))
        signIn(username, password).onFailure { biometricStore.clear() }.getOrThrow()
    }

    /**
     * Signs in with the credentials kept for the fingerprint sign-in.
     *
     * When the server rejects them (the password was changed), the fingerprint sign-in is turned off
     * so the user signs in with the new password and activates it again.
     *
     * @param cipher decryption cipher authorized by the biometric prompt.
     */
    fun loginWithBiometric(cipher: Cipher) = execute {
        val credentials = biometricStore.read(cipher)
        if (credentials == null) {
            biometricStore.clear()
            error("No se pudo leer el ingreso con huella. Ingresa con tu contraseña y vuelve a activarlo.")
        }
        signIn(credentials.username, credentials.password).onFailure { exception ->
            if (exception is IllegalStateException) {
                biometricStore.clear()
                error("Tu contraseña cambió. Ingresa con la nueva y vuelve a activar el ingreso con huella.")
            }
        }.getOrThrow()
    }

    /**
     * Shows a message in the login error banner.
     *
     * @param message text to show.
     */
    fun showError(message: String) { _error.value = message }

    fun biometricEnabled() = repository.isBiometricEnabled()
    fun setBiometricEnabled(value: Boolean) = repository.setBiometricEnabled(value)
    private fun execute(block: suspend () -> Unit) { viewModelScope.launch { _loading.value = true; clearFeedback(); try { block() } catch (e: Exception) { _error.value = e.message ?: "Ocurrió un error" } finally { _loading.value = false } } }
    companion object { fun validPassword(s: String) = s.length >= 8 && s.any(Char::isUpperCase) && s.any(Char::isLowerCase) && s.any(Char::isDigit) && s.any { !it.isLetterOrDigit() } }
}
