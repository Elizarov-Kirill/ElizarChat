package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {

    // === ОСНОВНЫЕ ОПЕРАЦИИ С ЧАТАМИ ===

    /**
     * POST /api/v1/chats
     * Создание чата
     */
    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): Response<CreateChatResponse>

    /**
     * GET /api/v1/chats
     * Получение списка чатов текущего пользователя
     */
    @GET("chats")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ChatsResponse>>

    /**
     * GET /api/v1/chats/{id}
     * Получение чата по ID
     */
    @GET("chats/{id}")
    suspend fun getChatById(
        @Path("id") id: Int
    ): Response<ApiResponse<ChatDto>>

}