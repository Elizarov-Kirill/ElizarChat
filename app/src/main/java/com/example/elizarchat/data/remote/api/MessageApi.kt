package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {

    /**
     * POST /api/v1/chats/{chatId}/messages
     * Отправка сообщения
     */
    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /**
     * GET /api/v1/chats/{chatId}/messages
     * Получение сообщений чата
     */
    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("before") before: Long? = null
    ): Response<ApiResponse<MessagesResponse>>

    /**
     * GET /api/v1/chats/{chatId}/messages/{id}
     * Получение конкретного сообщения
     */
    @GET("chats/{chatId}/messages/{id}")
    suspend fun getMessage(
        @Path("chatId") chatId: String,
        @Path("id") id: String
    ): Response<ApiResponse<MessageDto>>

    /**
     * PUT /api/v1/chats/{chatId}/messages/{id}
     * Редактирование сообщения
     */
    @PUT("chats/{chatId}/messages/{id}")
    suspend fun updateMessage(
        @Path("chatId") chatId: String,
        @Path("id") id: String,
        @Body request: UpdateMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /**
     * DELETE /api/v1/chats/{chatId}/messages/{id}
     * Удаление сообщения
     */
    @DELETE("chats/{chatId}/messages/{id}")
    suspend fun deleteMessage(
        @Path("chatId") chatId: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    /**
     * POST /api/v1/chats/{chatId}/messages/read
     * Отметка сообщений как прочитанных
     */
    @POST("chats/{chatId}/messages/read")
    suspend fun markAsRead(
        @Path("chatId") chatId: String,
        @Body request: MarkAsReadRequest
    ): Response<ApiResponse<Unit>>
}