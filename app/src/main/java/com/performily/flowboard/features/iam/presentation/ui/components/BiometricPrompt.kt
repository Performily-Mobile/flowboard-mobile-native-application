package com.performily.flowboard.features.iam.presentation.ui.components

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/**
 * True when the device has a strong biometric (fingerprint or equivalent) enrolled and ready.
 *
 * @receiver any context of the app.
 */
internal fun Context.canUseStrongBiometric(): Boolean =
    BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Activity that hosts the screen, needed to show the biometric prompt.
 *
 * @receiver context of a composable.
 * @return the hosting [FragmentActivity], or null when there is none.
 */
internal tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

/**
 * Shows the system biometric prompt bound to [cipher], so the cipher can only be used after the
 * user's fingerprint is accepted.
 *
 * Cancelling the prompt or choosing "Usar contraseña" ends silently; other failures are reported
 * through [onError].
 *
 * @receiver activity that hosts the prompt.
 * @param title title of the prompt.
 * @param subtitle short explanation under the title.
 * @param cipher cipher to authorize.
 * @param onSuccess called with the authorized cipher.
 * @param onError called with a message when the prompt fails.
 */
internal fun FragmentActivity.showBiometricPrompt(
    title: String,
    subtitle: String,
    cipher: Cipher,
    onSuccess: (Cipher) -> Unit,
    onError: (String) -> Unit
) {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val authorized = result.cryptoObject?.cipher
            if (authorized != null) onSuccess(authorized) else onError("No se pudo verificar tu huella. Intenta de nuevo.")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == BiometricPrompt.ERROR_CANCELED
            if (!cancelled) onError(errString.toString())
        }
    }
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Usar contraseña")
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .build()
    BiometricPrompt(this, ContextCompat.getMainExecutor(this), callback)
        .authenticate(info, BiometricPrompt.CryptoObject(cipher))
}
