package com.example.elizarchat

import android.app.Application
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager

class ElizarChatApplication : Application() {
    lateinit var apiManager: ApiManager
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager.getInstance(this)
        apiManager = ApiManager(this)
    }
}