package com.example.elizarchat.data.remote.config

import com.example.elizarchat.data.remote.api.*
import retrofit2.Retrofit

class ApiClient private constructor(
    private val retrofit: Retrofit
) {

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null

        fun getInstance(retrofit: Retrofit): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(retrofit).also { INSTANCE = it }
            }
        }
    }

    // Ленивая инициализация API сервисов
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val chatApi: ChatApi by lazy { retrofit.create(ChatApi::class.java) }
    val messageApi: MessageApi by lazy { retrofit.create(MessageApi::class.java) }
}

// Расширение для удобства
fun Retrofit.createApiClient(): ApiClient = ApiClient.getInstance(this)