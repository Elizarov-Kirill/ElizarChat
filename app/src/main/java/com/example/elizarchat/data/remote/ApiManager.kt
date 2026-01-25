package com.example.elizarchat.data.remote

import android.content.Context
import com.example.elizarchat.AppConstants
import com.example.elizarchat.data.remote.api.*
import com.example.elizarchat.data.remote.config.RetrofitConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiManager(context: Context) {
    private val sharedPrefs = context.getSharedPreferences(
        AppConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Провайдер токена для интерцептора
    private val tokenProvider: () -> String? = {
        sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
    }

    // Retrofit экземпляры
    private val unauthenticatedRetrofit = RetrofitConfig.createUnauthenticatedRetrofit()
    private val authenticatedRetrofit = RetrofitConfig.createAuthenticatedRetrofit(tokenProvider)

    // API интерфейсы
    val authApi: AuthApi by lazy {
        unauthenticatedRetrofit.create(AuthApi::class.java)
    }

    val systemApi: SystemApi by lazy {
        unauthenticatedRetrofit.create(SystemApi::class.java)
    }

    val userApi: UserApi by lazy {
        authenticatedRetrofit.create(UserApi::class.java)
    }

    val chatApi: ChatApi by lazy {
        authenticatedRetrofit.create(ChatApi::class.java)
    }

    val messageApi: MessageApi by lazy {
        authenticatedRetrofit.create(MessageApi::class.java)
    }

    // Управление токенами
    fun saveTokens(accessToken: String, refreshToken: String) {
        with(sharedPrefs.edit()) {
            putString(AppConstants.KEY_ACCESS_TOKEN, accessToken)
            putString(AppConstants.KEY_REFRESH_TOKEN, refreshToken)
            // Время истечения токена (15 минут)
            val expiryTime = System.currentTimeMillis() +
                    (AppConstants.ACCESS_TOKEN_LIFETIME_MINUTES * 60 * 1000)
            putLong(AppConstants.KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }

    fun clearTokens() {
        with(sharedPrefs.edit()) {
            remove(AppConstants.KEY_ACCESS_TOKEN)
            remove(AppConstants.KEY_REFRESH_TOKEN)
            remove(AppConstants.KEY_TOKEN_EXPIRY)
            remove(AppConstants.KEY_USER_ID)
            remove(AppConstants.KEY_USERNAME)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPrefs.getString(AppConstants.KEY_REFRESH_TOKEN, null)
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = sharedPrefs.getLong(AppConstants.KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() > expiryTime
    }

    fun saveUserInfo(userId: Long, username: String) {
        with(sharedPrefs.edit()) {
            putLong(AppConstants.KEY_USER_ID, userId)
            putString(AppConstants.KEY_USERNAME, username)
            apply()
        }
    }

    fun getUserId(): Long {
        return sharedPrefs.getLong(AppConstants.KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return sharedPrefs.getString(AppConstants.KEY_USERNAME, null)
    }

    suspend fun refreshAccessToken(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = getRefreshToken() ?: return@withContext false
                val request = com.example.elizarchat.data.remote.dto.RefreshTokenRequest(refreshToken)
                val response = authApi.refreshToken(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.data?.let { tokenResponse ->
                        saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                        true
                    } ?: false
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}