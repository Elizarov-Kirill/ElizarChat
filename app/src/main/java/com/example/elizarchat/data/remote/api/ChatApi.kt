package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.ChatWithParticipantsDto
import com.example.elizarchat.data.remote.dto.CreateChatRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChatApi {
    @GET("chats")
    suspend fun getChats(): Response<List<ChatDto>>

    @GET("chats/{id}")
    suspend fun getChat(@Path("id") id: Long): Response<ChatWithParticipantsDto>

    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequestDto): Response<ChatWithParticipantsDto>

    @PUT("chats/{id}")
    suspend fun updateChat(@Path("id") id: Long, @Body chat: ChatDto): Response<ChatDto>

    @DELETE("chats/{id}")
    suspend fun deleteChat(@Path("id") id: Long): Response<Unit>
}