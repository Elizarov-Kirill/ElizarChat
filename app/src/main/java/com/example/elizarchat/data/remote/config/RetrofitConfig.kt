package com.example.elizarchat.data.remote.config

import com.example.elizarchat.AppConstants
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
        // УБРАНО: prettyPrint = true // Может конфликтовать с сервером
    }

    private val contentType = "application/json".toMediaType()

    // Клиент без авторизации
    fun createUnauthenticatedClient(): OkHttpClient {
        println("=== RetrofitConfig: Создание клиента ===")
        println("Base URL из AppConstants: ${AppConstants.API_BASE_URL}")

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (AppConstants.Debug.LOG_NETWORK) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
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
            level = if (AppConstants.Debug.LOG_NETWORK) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
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

    // Retrofit экземпляры - ИСПРАВЛЕНО: используем API_BASE_URL напрямую
    fun createUnauthenticatedRetrofit(): Retrofit {
        println("=== RetrofitConfig: Создание Retrofit ===")
        println("Используемый baseUrl: ${AppConstants.API_BASE_URL}")

        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL) // УБРАНА функция getBaseUrl()
            .client(createUnauthenticatedClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    fun createAuthenticatedRetrofit(
        tokenProvider: () -> String?
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL) // УБРАНА функция getBaseUrl()
            .client(createAuthenticatedClient(tokenProvider))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // Интерцепторы
    private class CommonHeadersInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "ElizaChat-Android/1.0.0")
                .build()
            println("Отправляется запрос к: ${chain.request().url}")
            return chain.proceed(request)
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
            try {
                val response = chain.proceed(chain.request())
                if (!response.isSuccessful) {
                    println("HTTP ошибка: ${response.code} - ${response.message}")
                } else {
                    // Логируем успешный ответ для отладки
                    val responseBody = response.peekBody(1024 * 1024) // 1MB
                    val responseString = responseBody.string()
                    println("✅ HTTP ответ (первые 1KB): ${responseString.take(1024)}")
                }
                return response
            } catch (e: Exception) {
                println("Сетевая ошибка: ${e.message}")
                throw e
            }
        }
    }
}