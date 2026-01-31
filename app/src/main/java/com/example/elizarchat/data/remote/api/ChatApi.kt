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
    ): Response<ApiResponse<ChatDto>>

    /**
     * GET /api/v1/chats
     * Получение списка чатов текущего пользователя
     */
    @GET("chats")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null,  // "private", "group", "channel"
        @Query("unread_only") unreadOnly: Boolean = false,
        @Query("pinned_only") pinnedOnly: Boolean = false
    ): Response<ApiResponse<ChatsResponse>>

    /**
     * GET /api/v1/chats/{id}
     * Получение чата по ID
     */
    @GET("chats/{chat_id}")
    suspend fun getChatById(
        @Path("chat_id") id: Int  // Изменено: String → Int, snake_case
    ): Response<ApiResponse<ChatDetailResponse>>

    /**
     * PUT /api/v1/chats/{id}
     * Обновление чата
     */
    @PUT("chats/{chat_id}")
    suspend fun updateChat(
        @Path("chat_id") id: Int,  // Изменено: String → Int, snake_case
        @Body request: UpdateChatRequest
    ): Response<ApiResponse<ChatDto>>

    /**
     * DELETE /api/v1/chats/{id}
     * Удаление чата
     */
    @DELETE("chats/{chat_id}")
    suspend fun deleteChat(
        @Path("chat_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    // === ПРИВАТНЫЕ ЧАТЫ ===

    /**
     * GET /api/v1/chats/private/{userId}
     * Получение или создание приватного чата с пользователем
     */
    @GET("chats/private/{user_id}")
    suspend fun getOrCreatePrivateChat(
        @Path("user_id") userId: Int  // Добавлено
    ): Response<ApiResponse<ChatDto>>

    /**
     * GET /api/v1/chats/private
     * Получение списка приватных чатов
     */
    @GET("chats/private")
    suspend fun getPrivateChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ChatsResponse>>

    // === УЧАСТНИКИ ЧАТА ===

    /**
     * GET /api/v1/chats/{id}/members
     * Получение участников чата
     */
    @GET("chats/{chat_id}/members")
    suspend fun getChatMembers(
        @Path("chat_id") id: Int,  // Изменено: String → Int, snake_case
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("role") role: String? = null  // "owner", "admin", "member", "guest"
    ): Response<ApiResponse<List<ChatMemberDto>>>

    /**
     * POST /api/v1/chats/{id}/members
     * Добавление участника в чат
     */
    @POST("chats/{chat_id}/members")
    suspend fun addMember(
        @Path("chat_id") id: Int,  // Изменено: String → Int, snake_case
        @Body request: AddMemberRequest
    ): Response<ApiResponse<Unit>>

    /**
     * POST /api/v1/chats/{id}/members/batch
     * Добавление нескольких участников
     */
    @POST("chats/{chat_id}/members/batch")
    suspend fun addMembers(
        @Path("chat_id") id: Int,  // Добавлено
        @Body request: AddMembersRequest
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/chats/{id}/members/{userId}
     * Обновление роли участника
     */
    @PUT("chats/{chat_id}/members/{user_id}")
    suspend fun updateMemberRole(
        @Path("chat_id") chatId: Int,  // Изменено: String → Int, snake_case
        @Path("user_id") userId: Int,  // Изменено: String → Int, snake_case
        @Body request: UpdateMemberRoleRequest
    ): Response<ApiResponse<Unit>>

    /**
     * DELETE /api/v1/chats/{id}/members
     * Удаление участника из чата
     */
    @DELETE("chats/{chat_id}/members")
    suspend fun removeMember(
        @Path("chat_id") id: Int,  // Изменено: String → Int, snake_case
        @Body request: RemoveMemberRequest
    ): Response<ApiResponse<Unit>>

    /**
     * DELETE /api/v1/chats/{id}/members/{userId}
     * Удаление участника по ID
     */
    @DELETE("chats/{chat_id}/members/{user_id}")
    suspend fun removeMemberById(
        @Path("chat_id") chatId: Int,  // Добавлено
        @Path("user_id") userId: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/chats/{id}/members/me/read
     * Отметка всех сообщений как прочитанных текущим пользователем
     */
    @PUT("chats/{chat_id}/members/me/read")
    suspend fun markChatAsRead(
        @Path("chat_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/chats/{id}/members/me/unread
     * Обновление счетчика непрочитанных для текущего пользователя
     */
    @PUT("chats/{chat_id}/members/me/unread")
    suspend fun updateUnreadCount(
        @Path("chat_id") id: Int,  // Добавлено
        @Body request: UpdateUnreadCountRequest
    ): Response<ApiResponse<Unit>>

    // === ПОИСК И ФИЛЬТРАЦИЯ ===

    /**
     * GET /api/v1/chats/search
     * Поиск чатов
     */
    @GET("chats/search")
    suspend fun searchChats(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null
    ): Response<ApiResponse<ChatsResponse>>

    /**
     * GET /api/v1/chats/unread
     * Получение чатов с непрочитанными сообщениями
     */
    @GET("chats/unread")
    suspend fun getUnreadChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ChatsResponse>>

    /**
     * PUT /api/v1/chats/{id}/pin
     * Закрепление/открепление чата
     */
    @PUT("chats/{chat_id}/pin")
    suspend fun togglePinChat(
        @Path("chat_id") id: Int,  // Добавлено
        @Query("pinned") pinned: Boolean = true
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/chats/{id}/mute
     * Включение/выключение уведомлений чата
     */
    @PUT("chats/{chat_id}/mute")
    suspend fun toggleMuteChat(
        @Path("chat_id") id: Int,  // Добавлено
        @Query("muted") muted: Boolean = true
    ): Response<ApiResponse<Unit>>
}

/**
 * Запрос для добавления нескольких участников
 */
@kotlinx.serialization.Serializable
data class AddMembersRequest(
    @kotlinx.serialization.SerialName("user_ids")
    val userIds: List<Int>,
    @kotlinx.serialization.SerialName("role")
    val role: String = "member"
)

/**
 * Запрос для обновления роли участника
 */
@kotlinx.serialization.Serializable
data class UpdateMemberRoleRequest(
    @kotlinx.serialization.SerialName("role")
    val role: String  // "owner", "admin", "member", "guest"
)