package com.performily.flowboard.features.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.iam.application.ChangePasswordUseCase
import com.performily.flowboard.features.iam.application.SignInUseCase
import com.performily.flowboard.features.iam.application.SignOutUseCase
import com.performily.flowboard.features.iam.domain.repository.IamRepository
import com.performily.flowboard.features.iam.domain.repository.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IamViewModel @Inject constructor(
    private val repository: IamRepository,
    private val signIn: SignInUseCase,
    private val change: ChangePasswordUseCase,
    private val signOut: SignOutUseCase
): ViewModel() {
    val session = repository.session
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
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
    fun resetPassword(id: Long) = execute { repository.resetPassword(id).getOrThrow(); _message.value = "Restablecimiento solicitado. Verifica el envío desde el backend." }
    fun biometricEnabled() = repository.isBiometricEnabled()
    fun setBiometricEnabled(value: Boolean) = repository.setBiometricEnabled(value)
    private fun execute(block: suspend () -> Unit) { viewModelScope.launch { _loading.value = true; clearFeedback(); try { block() } catch (e: Exception) { _error.value = e.message ?: "Ocurrió un error" } finally { _loading.value = false } } }
    companion object { fun validPassword(s: String) = s.length >= 8 && s.any(Char::isUpperCase) && s.any(Char::isLowerCase) && s.any(Char::isDigit) && s.any { !it.isLetterOrDigit() } }
}
