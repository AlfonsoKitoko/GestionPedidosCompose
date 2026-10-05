package com.alfonsokitoko.gestionpedidos.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
	val email: String,
	val password: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
	@Json(name = "_id")
	val id: String,
	val email: String,
	val token: String
)