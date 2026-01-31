package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {

    /**
     * POST /api/v1/chats/{chatId}/messages
     * Отправка сообщения
     */
    @POST("chats/{chat_id}/messages")
    suspend fun sendMessage(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /**
     * GET /api/v1/chats/{chatId}/messages
     * Получение сообщений чата
     */
    @GET("chats/{chat_id}/messages")
    suspend fun getMessages(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("before") before: Long? = null,  // timestamp в миллисекундах
        @Query("after") after: Long? = null,  // Добавлено: сообщения после timestamp
        @Query("include_deleted") includeDeleted: Boolean = false  // Добавлено
    ): Response<ApiResponse<MessagesResponse>>

    /**
     * GET /api/v1/chats/{chatId}/messages/{id}
     * Получение конкретного сообщения
     */
    @GET("chats/{chat_id}/messages/{message_id}")
    suspend fun getMessage(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Path("message_id") messageId: Int  // Изменено: String → Int, snake_case
    ): Response<ApiResponse<MessageDto>>

    /**
     * PUT /api/v1/chats/{chatId}/messages/{id}
     * Редактирование сообщения
     */
    @PUT("chats/{chat_id}/messages/{message_id}")
    suspend fun updateMessage(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Path("message_id") messageId: Int,  // Изменено: String → Int, snake_case
        @Body request: UpdateMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /**
     * DELETE /api/v1/chats/{chatId}/messages/{id}
     * Удаление сообщения (мягкое удаление)
     */
    @DELETE("chats/{chat_id}/messages/{message_id}")
    suspend fun deleteMessage(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Path("message_id") messageId: Int  // Изменено: String → Int, snake_case
    ): Response<ApiResponse<Unit>>

    /**
     * POST /api/v1/chats/{chatId}/messages/read
     * Отметка сообщений как прочитанных
     */
    @POST("chats/{chat_id}/messages/read")
    suspend fun markAsRead(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Body request: MarkAsReadRequest
    ): Response<ApiResponse<Unit>>

    /**
     * POST /api/v1/chats/{chatId}/messages/batch-read
     * Отметка нескольких сообщений как прочитанных
     */
    @POST("chats/{chat_id}/messages/batch-read")
    suspend fun markMessagesAsRead(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Body request: MarkMessagesAsReadRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    /**
     * GET /api/v1/chats/{chatId}/messages/search
     * Поиск сообщений в чате
     */
    @GET("chats/{chat_id}/messages/search")
    suspend fun searchMessages(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<MessagesResponse>>  // Добавлено

    /**
     * PUT /api/v1/chats/{chatId}/messages/{messageId}/reaction
     * Добавление реакции к сообщению
     */
    @PUT("chats/{chat_id}/messages/{message_id}/reaction")
    suspend fun addReaction(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Path("message_id") messageId: Int,  // Добавлено
        @Body request: AddReactionRequest
    ): Response<ApiResponse<MessageDto>>  // Добавлено

    /**
     * DELETE /api/v1/chats/{chatId}/messages/{messageId}/reaction
     * Удаление реакции с сообщения
     */
    @DELETE("chats/{chat_id}/messages/{message_id}/reaction")
    suspend fun removeReaction(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Path("message_id") messageId: Int,  // Добавлено
        @Query("reaction_type") reactionType: String  // Добавлено
    ): Response<ApiResponse<MessageDto>>  // Добавлено

    /**
     * POST /api/v1/chats/{chatId}/messages/{messageId}/forward
     * Пересылка сообщения
     */
    @POST("chats/{chat_id}/messages/{message_id}/forward")
    suspend fun forwardMessage(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Path("message_id") messageId: Int,  // Добавлено
        @Body request: ForwardMessageRequest
    ): Response<ApiResponse<List<MessageDto>>>  // Добавлено
}

/**
 * Запрос для отметки нескольких сообщений как прочитанных
 */
@kotlinx.serialization.Serializable
data class MarkMessagesAsReadRequest(
    @kotlinx.serialization.SerialName("message_ids")
    val messageIds: List<Int>  // Добавлено
)

/**
 * Запрос для добавления реакции
 */
@kotlinx.serialization.Serializable
data class AddReactionRequest(
    @kotlinx.serialization.SerialName("reaction_type")
    val reactionType: String,
    @kotlinx.serialization.SerialName("emoji")
    val emoji: String? = null
)

/**
 * Запрос для пересылки сообщения
 */
@kotlinx.serialization.Serializable
data class ForwardMessageRequest(
    @kotlinx.serialization.SerialName("chat_ids")
    val chatIds: List<Int>,
    @kotlinx.serialization.SerialName("add_comment")
    val addComment: Boolean = false,
    @kotlinx.serialization.SerialName("comment")
    val comment: String? = null
)