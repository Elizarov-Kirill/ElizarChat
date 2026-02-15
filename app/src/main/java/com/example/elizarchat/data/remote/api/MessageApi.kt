package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {

    /**
     * GET /api/v1/chats/{chatId}/messages
     * Получение сообщений чата
     * Ответ: { success: true, messages: List<MessageDto>, pagination: {...} }
     */
    @GET("chats/{chat_id}/messages")
    suspend fun getMessages(
        @Path("chat_id") chatId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("before") before: String? = null
    ): Response<ApiResponse<MessagesResponse>>  // ApiResponse содержит поля messages и pagination

    /**
     * POST /api/v1/chats/{chatId}/messages
     * Отправка сообщения
     * Ответ: { success: true, message: MessageDto }
     */
    @POST("chats/{chat_id}/messages")
    suspend fun sendMessage(
        @Path("chat_id") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /**
     * POST /api/v1/chats/{chatId}/messages/read
     * Отметка сообщений как прочитанных
     */
    @POST("chats/{chat_id}/messages/read")
    suspend fun markAsRead(
        @Path("chat_id") chatId: Int,
        @Body request: MarkAsReadRequest
    ): Response<ApiResponse<Unit>>
}