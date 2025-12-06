package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.dto.MessagesResponseDto
import com.example.elizarchat.data.remote.dto.SendMessageRequestDto
import com.example.elizarchat.data.remote.dto.SendMessageResponseDto
import com.example.elizarchat.data.remote.dto.UpdateMessageStatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {
    @GET("messages")
    suspend fun getMessages(
        @Query("chatId") chatId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<MessagesResponseDto>

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequestDto): Response<SendMessageResponseDto>

    @PUT("messages/{id}/status")
    suspend fun updateMessageStatus(
        @Path("id") messageId: Long,
        @Body request: UpdateMessageStatusRequestDto
    ): Response<Unit>

    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") messageId: Long): Response<Unit>
}