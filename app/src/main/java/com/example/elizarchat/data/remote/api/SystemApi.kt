package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface SystemApi {

    /**
     * GET /api/v1/health
     * Проверка работоспособности API
     */
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
}