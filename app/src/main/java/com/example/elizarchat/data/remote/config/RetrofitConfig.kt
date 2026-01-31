package com.example.elizarchat.data.remote.config

import android.content.Context
import com.example.elizarchat.AppConstants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitConfig {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true  // ← Убрали BuildConfig.DEBUG
    }

    private val contentType = "application/json".toMediaType()

    // Клиент без авторизации (для регистрации/входа)
    fun createUnauthenticatedClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug()) {  // ← Используем функцию isDebug()
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(AppConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(CommonHeadersInterceptor())
            .build()
    }

    // Клиент с авторизацией
    fun createAuthenticatedClient(
        tokenProvider: () -> String?
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug()) {  // ← Используем функцию isDebug()
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(AppConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(CommonHeadersInterceptor())
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(ErrorInterceptor())
            .build()
    }

    // Retrofit экземпляры
    fun createUnauthenticatedRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL)
            .client(createUnauthenticatedClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    fun createAuthenticatedRetrofit(
        tokenProvider: () -> String?
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL)
            .client(createAuthenticatedClient(tokenProvider))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // Вспомогательная функция для проверки debug режима
    private fun isDebug(): Boolean {
        return try {
            // Пытаемся использовать BuildConfig, если доступен
            Class.forName("com.example.elizarchat.BuildConfig")
                .getDeclaredField("DEBUG")
                .get(null) as Boolean
        } catch (e: Exception) {
            // Если BuildConfig недоступен, используем AppConstants.Debug флаги
            com.example.elizarchat.AppConstants.Debug.LOG_NETWORK
        }
    }

    // Функция для получения версии приложения
    private fun getAppVersion(): String {
        return try {
            Class.forName("com.example.elizarchat.BuildConfig")
                .getDeclaredField("VERSION_NAME")
                .get(null) as? String ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // Интерцепторы
    private class CommonHeadersInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "ElizaChat-Android/${getAppVersionFromContext()}")
                .build()
            return chain.proceed(request)
        }

        private fun getAppVersionFromContext(): String {
            return try {
                // Альтернативный способ получить версию
                Class.forName("com.example.elizarchat.BuildConfig")
                    .getDeclaredField("VERSION_NAME")
                    .get(null) as? String ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }
        }
    }

    private class AuthInterceptor(
        private val tokenProvider: () -> String?
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = tokenProvider()

            val requestBuilder = originalRequest.newBuilder()
            token?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }

            return chain.proceed(requestBuilder.build())
        }
    }

    private class ErrorInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            if (!response.isSuccessful) {
                // Можно добавить логику обработки ошибок
                // Например, при 401 - refresh токена
                when (response.code) {
                    401 -> {
                        // Токен истек или невалиден
                        // Здесь можно вызвать refresh токена
                    }
                    403 -> {
                        // Доступ запрещен
                    }
                    429 -> {
                        // Слишком много запросов
                    }
                    500 -> {
                        // Ошибка сервера
                    }
                }
            }

            return response
        }
    }
}