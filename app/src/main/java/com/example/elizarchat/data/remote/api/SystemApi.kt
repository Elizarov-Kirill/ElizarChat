package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.ApiResponse
import com.example.elizarchat.data.remote.dto.ServerInfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface SystemApi {

    /**
     * GET /api/v1/system/info
     * Получение информации о сервере
     */
    @GET("system/info")
    suspend fun getServerInfo(): Response<ApiResponse<ServerInfoResponse>>

    /**
     * GET /api/v1/system/stats
     * Получение статистики сервера
     */
    @GET("system/stats")
    suspend fun getStats(): Response<ApiResponse<SystemStatsResponse>>

    /**
     * GET /api/v1/system/ping
     * Проверка задержки
     */
    @GET("system/ping")
    suspend fun ping(): Response<ApiResponse<PingResponse>>
}

// === DTO ДЛЯ SYSTEM API ===

@kotlinx.serialization.Serializable
data class SystemStatsResponse(
    @kotlinx.serialization.SerialName("users_count")
    val usersCount: Int,
    @kotlinx.serialization.SerialName("online_users")
    val onlineUsers: Int,
    @kotlinx.serialization.SerialName("total_chats")
    val totalChats: Int,
    @kotlinx.serialization.SerialName("total_messages")
    val totalMessages: Long,
    @kotlinx.serialization.SerialName("uptime")
    val uptime: Long,  // в секундах
    @kotlinx.serialization.SerialName("memory_usage")
    val memoryUsage: MemoryUsage? = null,
    @kotlinx.serialization.SerialName("database_stats")
    val databaseStats: DatabaseStats? = null
)

@kotlinx.serialization.Serializable
data class MemoryUsage(
    @kotlinx.serialization.SerialName("total")
    val total: Long,
    @kotlinx.serialization.SerialName("used")
    val used: Long,
    @kotlinx.serialization.SerialName("free")
    val free: Long
)

@kotlinx.serialization.Serializable
data class DatabaseStats(
    @kotlinx.serialization.SerialName("connection_count")
    val connectionCount: Int,
    @kotlinx.serialization.SerialName("query_count")
    val queryCount: Long,
    @kotlinx.serialization.SerialName("latency_ms")
    val latencyMs: Double
)

@kotlinx.serialization.Serializable
data class PingResponse(
    @kotlinx.serialization.SerialName("timestamp")
    val timestamp: String,
    @kotlinx.serialization.SerialName("latency_ms")
    val latencyMs: Long
)