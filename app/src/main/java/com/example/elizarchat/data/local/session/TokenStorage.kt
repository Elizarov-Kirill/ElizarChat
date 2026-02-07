package com.example.elizarchat.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Date

// Создаем DataStore
val Context.tokenDataStore by preferencesDataStore(name = "token_storage")

class TokenStorage(private val context: Context) {

    companion object {
        // Ключи для хранения данных
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACCESS_TOKEN_EXPIRY_KEY = longPreferencesKey("access_token_expiry")
        private val REFRESH_TOKEN_EXPIRY_KEY = longPreferencesKey("refresh_token_expiry")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")

        // Время жизни refresh токена (30 дней как на сервере)
        private const val REFRESH_TOKEN_LIFETIME_MS = 30L * 24 * 60 * 60 * 1000L

        // ВРЕМЕННО: фиксированное время для access токена (15 минут)
        private const val TEMP_ACCESS_TOKEN_LIFETIME_MS = 15 * 60 * 1000L
    }

    /**
     * Сохраняет токены аутентификации
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String? = null
    ) {
        println("💾 DEBUG TokenStorage.saveTokens(): Начало сохранения")
        println("💾 DEBUG TokenStorage.saveTokens(): accessToken длина: ${accessToken.length}")
        println("💾 DEBUG TokenStorage.saveTokens(): refreshToken: $refreshToken")
        println("💾 DEBUG TokenStorage.saveTokens(): userId: $userId")

        context.tokenDataStore.edit { preferences ->
            val now = System.currentTimeMillis()
            val accessExpiry = now + TEMP_ACCESS_TOKEN_LIFETIME_MS
            val refreshExpiry = now + REFRESH_TOKEN_LIFETIME_MS

            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[ACCESS_TOKEN_EXPIRY_KEY] = accessExpiry
            preferences[REFRESH_TOKEN_EXPIRY_KEY] = refreshExpiry
            preferences[IS_LOGGED_IN_KEY] = true

            userId?.let {
                preferences[USER_ID_KEY] = it
            }

            println("💾 DEBUG TokenStorage.saveTokens(): Данные сохранены")
            println("💾 DEBUG TokenStorage.saveTokens(): accessExpiry: $accessExpiry")
            println("💾 DEBUG TokenStorage.saveTokens(): refreshExpiry: $refreshExpiry")
        }
    }

    /**
     * Получает access токен
     */
    suspend fun getAccessToken(): String? {
        return try {
            val preferences = context.tokenDataStore.data.first()
            preferences[ACCESS_TOKEN_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Получает refresh токен
     */
    suspend fun getRefreshToken(): String? {
        return try {
            val preferences = context.tokenDataStore.data.first()
            preferences[REFRESH_TOKEN_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Получает ID пользователя
     */
    suspend fun getUserId(): String? {
        return try {
            val preferences = context.tokenDataStore.data.first()
            preferences[USER_ID_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверяет, истек ли access токен (по сохраненному времени)
     */
    suspend fun isAccessTokenExpired(): Boolean {
        return try {
            val preferences = context.tokenDataStore.data.first()
            val expiryTime = preferences[ACCESS_TOKEN_EXPIRY_KEY] ?: 0L
            System.currentTimeMillis() > expiryTime
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Проверяет, истек ли refresh токен (по сохраненному времени)
     */
    suspend fun isRefreshTokenExpired(): Boolean {
        return try {
            val preferences = context.tokenDataStore.data.first()
            val expiryTime = preferences[REFRESH_TOKEN_EXPIRY_KEY] ?: 0L
            System.currentTimeMillis() > expiryTime
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Обновляет access токен (без изменения refresh токена)
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        context.tokenDataStore.edit { preferences ->
            val now = System.currentTimeMillis()

            // ⚠️ ВРЕМЕННО: используем фиксированное время
            val accessExpiry = now + TEMP_ACCESS_TOKEN_LIFETIME_MS

            preferences[ACCESS_TOKEN_KEY] = newAccessToken
            preferences[ACCESS_TOKEN_EXPIRY_KEY] = accessExpiry
        }
    }

    /**
     * Проверяет, нужно ли обновить токен, и возвращает refresh токен если нужно
     */
    suspend fun shouldRefreshToken(): String? {
        return if (isAccessTokenExpired() && !isRefreshTokenExpired()) {
            getRefreshToken()
        } else {
            null
        }
    }

    /**
     * Очищает все сохраненные токены (логаут)
     */
    suspend fun clearTokens() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(ACCESS_TOKEN_EXPIRY_KEY)
            preferences.remove(REFRESH_TOKEN_EXPIRY_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    /**
     * Проверяет, авторизован ли пользователь
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            val preferences = context.tokenDataStore.data.first()
            val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
            val hasAccessToken = preferences[ACCESS_TOKEN_KEY] != null
            val hasRefreshToken = preferences[REFRESH_TOKEN_KEY] != null
            val refreshNotExpired = !isRefreshTokenExpired()

            isLoggedIn && hasAccessToken && hasRefreshToken && refreshNotExpired
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Flow для отслеживания статуса авторизации
     */
    val isLoggedInFlow: Flow<Boolean> = context.tokenDataStore.data
        .map { preferences ->
            val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
            val hasAccessToken = preferences[ACCESS_TOKEN_KEY] != null
            val hasRefreshToken = preferences[REFRESH_TOKEN_KEY] != null
            val refreshNotExpired = !isRefreshTokenExpired()

            isLoggedIn && hasAccessToken && hasRefreshToken && refreshNotExpired
        }

    /**
     * Flow для отслеживания ID пользователя
     */
    val userIdFlow: Flow<String?> = context.tokenDataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    // Синхронные методы для использования в blocking контекстах
    fun getAccessTokenSync(): String? = runBlocking { getAccessToken() }
    fun getRefreshTokenSync(): String? = runBlocking { getRefreshToken() }
    fun isLoggedInSync(): Boolean = runBlocking { isLoggedIn() }
    fun clearTokensSync() = runBlocking { clearTokens() }

    /**
     * Получает информацию о времени истечения токенов
     */
    suspend fun getTokenInfo(): TokenInfo {
        return try {
            val preferences = context.tokenDataStore.data.first()
            val accessExpiry = preferences[ACCESS_TOKEN_EXPIRY_KEY] ?: 0L
            val refreshExpiry = preferences[REFRESH_TOKEN_EXPIRY_KEY] ?: 0L

            TokenInfo(
                accessTokenExpiresAt = Date(accessExpiry),
                refreshTokenExpiresAt = Date(refreshExpiry),
                isAccessTokenExpired = isAccessTokenExpired(),
                isRefreshTokenExpired = isRefreshTokenExpired(),
                userId = getUserId()
            )
        } catch (e: Exception) {
            TokenInfo()
        }
    }

    /**
     * Data класс для информации о токенах
     */
    data class TokenInfo(
        val accessTokenExpiresAt: Date? = null,
        val refreshTokenExpiresAt: Date? = null,
        val isAccessTokenExpired: Boolean = true,
        val isRefreshTokenExpired: Boolean = true,
        val userId: String? = null
    )
}