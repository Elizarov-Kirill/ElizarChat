package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {

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
    suspend fun getChats(): Response<ApiResponse<List<ChatDto>>>

    /**
     * GET /api/v1/chats/{id}
     * Получение чата по ID
     */
    @GET("chats/{id}")
    suspend fun getChatById(
        @Path("id") id: String
    ): Response<ApiResponse<ChatDto>>

    /**
     * PUT /api/v1/chats/{id}
     * Обновление чата
     */
    @PUT("chats/{id}")
    suspend fun updateChat(
        @Path("id") id: String,
        @Body request: UpdateChatRequest
    ): Response<ApiResponse<ChatDto>>

    /**
     * POST /api/v1/chats/{id}/members
     * Добавление участника в чат
     */
    @POST("chats/{id}/members")
    suspend fun addMember(
        @Path("id") id: String,
        @Body request: AddMemberRequest
    ): Response<ApiResponse<Unit>>

    /**
     * DELETE /api/v1/chats/{id}/members
     * Удаление участника из чата
     */
    @DELETE("chats/{id}/members")
    suspend fun removeMember(
        @Path("id") id: String,
        @Body request: RemoveMemberRequest
    ): Response<ApiResponse<Unit>>
}