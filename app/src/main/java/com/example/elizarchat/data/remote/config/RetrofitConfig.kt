package com.example.elizarchat.data.remote.config

import com.example.elizarchat.AppConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitConfig {
    fun create(token: String? = null): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL) // Используем HTTPS
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient(token))
            .build()
    }

    private fun createOkHttpClient(token: String?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(AppConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)

        // Добавляем токен аутентификации
        token?.let {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $it")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
        }

        // Добавляем заголовки даже без токена
        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestWithHeaders = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", AppConstants.USER_AGENT)
                .build()
            chain.proceed(requestWithHeaders)
        }

        // Логирование только в debug режиме
        if (AppConstants.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }
}