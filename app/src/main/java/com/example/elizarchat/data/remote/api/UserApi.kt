package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*  // Добавьте импорт всех DTO
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserResponseDto>  // ✅ ПРАВИЛЬНО

    @PUT("users/me")
    suspend fun updateProfile(@Body user: UserDto): Response<UserDto>

    // ИСПРАВЛЕНО: теперь возвращает UsersResponseDto вместо List<UserDto>
    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<UsersResponseDto>  // ✅ ИСПРАВЛЕНО!
}