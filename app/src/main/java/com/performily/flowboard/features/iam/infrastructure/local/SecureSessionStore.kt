package com.performily.flowboard.features.iam.infrastructure.local

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.performily.flowboard.features.iam.domain.entity.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.inject.Inject
import javax.inject.Singleton

/** AES-GCM key in AndroidKeyStore. The JWT is never saved as plain text. */
@Singleton
class SecureSessionStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("flowboard_iam_secure", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val alias = "flowboard_session_key"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
        return generator.generateKey()
    }
    fun save(session: AuthSession) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(gson.toJson(session).toByteArray(Charsets.UTF_8))
        prefs.edit().putString("jwt", Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)).apply()
    }
    fun read(): AuthSession? { return try {
        val data = Base64.decode(prefs.getString("jwt", null) ?: return null, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, data.copyOfRange(0, 12)))
        gson.fromJson(String(cipher.doFinal(data.copyOfRange(12, data.size)), Charsets.UTF_8), AuthSession::class.java)
    } catch (_: Exception) { clear(); null }
    }
    fun clear() { prefs.edit().clear().apply() }
    fun biometricEnabled() = prefs.getBoolean("biometric", false)
    fun setBiometricEnabled(enabled: Boolean) { prefs.edit().putBoolean("biometric", enabled).apply() }
}
