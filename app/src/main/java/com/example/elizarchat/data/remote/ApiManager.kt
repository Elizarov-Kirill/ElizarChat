package com.example.elizarchat.data.remote

import com.example.elizarchat.data.remote.api.*
import com.example.elizarchat.data.remote.config.RetrofitConfig

class ApiManager(private val token: String? = null) {
    private val retrofit = RetrofitConfig.create(token)

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val chatApi: ChatApi by lazy { retrofit.create(ChatApi::class.java) }
    val messageApi: MessageApi by lazy { retrofit.create(MessageApi::class.java) }

    fun updateToken(newToken: String): ApiManager {
        return ApiManager(newToken)
    }
}