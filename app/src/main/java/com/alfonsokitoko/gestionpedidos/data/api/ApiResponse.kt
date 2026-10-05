package com.alfonsokitoko.gestionpedidos.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
	val success: Boolean,
	val data: T,
	val message: String? = null
)
