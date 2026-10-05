package com.alfonsokitoko.gestionpedidos.data.api

import com.alfonsokitoko.gestionpedidos.data.model.LoginRequest
import com.alfonsokitoko.gestionpedidos.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
	@POST("auth/login")
	suspend fun login(@Body request: LoginRequest): ApiResponse<UserResponse>
}