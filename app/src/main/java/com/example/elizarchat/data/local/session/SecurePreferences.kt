package com.example.elizarchat.data.local.session

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Безопасное хранилище на основе EncryptedSharedPreferences
 * Защищает токен от извлечения при рутовом доступе
 */
class SecurePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "elizarchat_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_LAST_LOGIN = "last_login"
    }

    private val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Token management
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    // User info
    fun saveUserInfo(userId: Long, username: String) {
        sharedPreferences.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            .apply()
    }

    fun getUserId(): Long = sharedPreferences.getLong(KEY_USER_ID, -1)

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    // Token expiration
    fun saveTokenExpiration(expiresAt: Long) {
        sharedPreferences.edit().putLong(KEY_EXPIRES_AT, expiresAt).apply()
    }

    fun getTokenExpiration(): Long = sharedPreferences.getLong(KEY_EXPIRES_AT, 0)

    fun isTokenExpired(): Boolean {
        val expiresAt = getTokenExpiration()
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt
    }

    // Session management
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun hasActiveSession(): Boolean {
        return getAccessToken() != null && !isTokenExpired()
    }

    fun getLastLoginTime(): Long = sharedPreferences.getLong(KEY_LAST_LOGIN, 0)

    /**
     * Проверяет, есть ли все необходимые данные для сессии
     */
    fun isValidSession(): Boolean {
        return hasActiveSession() && getUserId() != -1L && getUsername() != null
    }
}