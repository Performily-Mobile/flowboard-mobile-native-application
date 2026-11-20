package com.performily.flowboard.features.iam.infrastructure.local

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Credentials kept for the fingerprint sign-in.
 *
 * @property username corporate e-mail of the account.
 * @property password password of the account.
 */
data class BiometricCredentials(val username: String, val password: String)

/**
 * Keeps the credentials of the fingerprint sign-in encrypted with an AndroidKeyStore key that can
 * only be used right after the user passes the biometric prompt.
 *
 * The key requires user authentication for every use and is invalidated when a new fingerprint is
 * enrolled, so the stored credentials cannot be read without the owner's finger. The data lives in
 * its own preferences file, apart from the session, so signing out keeps the fingerprint sign-in.
 */
@Singleton
class BiometricCredentialStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /** True when the fingerprint sign-in was activated on this device. */
    fun hasCredentials(): Boolean = prefs.contains(KEY_DATA) && prefs.contains(KEY_IV)

    /**
     * Cipher ready to encrypt the credentials once the biometric prompt authorizes it.
     *
     * @return the cipher, or null when the device has no secure lock screen or no enrolled biometrics.
     */
    fun encryptCipher(): Cipher? = try {
        newCipher().apply { init(Cipher.ENCRYPT_MODE, key()) }
    } catch (_: KeyPermanentlyInvalidatedException) {
        deleteKey()
        runCatching { newCipher().apply { init(Cipher.ENCRYPT_MODE, key()) } }.getOrNull()
    } catch (_: Exception) {
        null
    }

    /**
     * Cipher ready to decrypt the stored credentials once the biometric prompt authorizes it.
     *
     * @return the cipher, or null when nothing is stored or a new fingerprint invalidated the key,
     * in which case the stored data is removed.
     */
    fun decryptCipher(): Cipher? {
        val iv = prefs.getString(KEY_IV, null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return null
        return try {
            newCipher().apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv)) }
        } catch (_: KeyPermanentlyInvalidatedException) {
            clear()
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Encrypts and stores the credentials.
     *
     * @param cipher encryption cipher already authorized by the biometric prompt.
     * @param credentials username and password to keep.
     */
    fun save(cipher: Cipher, credentials: BiometricCredentials) {
        val encrypted = cipher.doFinal(gson.toJson(credentials).toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString(KEY_DATA, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Decrypts the stored credentials.
     *
     * @param cipher decryption cipher already authorized by the biometric prompt.
     * @return the credentials, or null when they cannot be read.
     */
    fun read(cipher: Cipher): BiometricCredentials? = runCatching {
        val data = Base64.decode(prefs.getString(KEY_DATA, null) ?: return null, Base64.NO_WRAP)
        gson.fromJson(String(cipher.doFinal(data), Charsets.UTF_8), BiometricCredentials::class.java)
    }.getOrNull()

    /** Turns the fingerprint sign-in off: removes the stored data and the key. */
    fun clear() {
        prefs.edit().clear().apply()
        deleteKey()
    }

    private fun newCipher(): Cipher = Cipher.getInstance(TRANSFORMATION)

    /**
     * Key of the fingerprint sign-in, created the first time.
     *
     * @return the AES key kept in AndroidKeyStore.
     */
    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) builder.setInvalidatedByBiometricEnrollment(true)
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            .apply { init(builder.build()) }
            .generateKey()
    }

    private fun deleteKey() {
        runCatching { KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }.deleteEntry(KEY_ALIAS) }
    }

    private companion object {
        const val PREFS_NAME = "flowboard_iam_biometric"
        const val KEY_DATA = "credentials"
        const val KEY_IV = "iv"
        const val KEY_ALIAS = "flowboard_biometric_key"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val KEY_SIZE = 256
    }
}
