package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.AuthResponseDto
import com.example.elizarchat.data.remote.dto.LoginRequestDto
import com.example.elizarchat.data.remote.dto.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}