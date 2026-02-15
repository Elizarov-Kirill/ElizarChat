package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {

    /**
     * POST /api/v1/chats
     * Создание чата
     * Ответ: { success: true, chat: ChatDto }
     */
    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): Response<CreateChatResponse>  // ApiResponse содержит поле chat

    /**
     * GET /api/v1/chats
     * Получение списка чатов текущего пользователя
     * Ответ: { success: true, chats: List<ChatDto> }
     */
    @GET("chats")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ChatsResponse>>  // ApiResponse содержит поле chats

    /**
     * GET /api/v1/chats/{id}
     * Получение чата по ID
     * Ответ: { success: true, chat: ChatDto }
     */
    @GET("chats/{id}")
    suspend fun getChatById(
        @Path("id") id: Int
    ): Response<ApiResponse<ChatDto>>
}