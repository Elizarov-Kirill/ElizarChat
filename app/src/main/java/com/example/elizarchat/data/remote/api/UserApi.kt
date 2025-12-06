package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.data.remote.dto.UsersResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Query

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body user: UserDto): Response<UserDto>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserDto>>
}