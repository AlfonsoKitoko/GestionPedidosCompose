package com.alfonsokitoko.gestionpedidos.data.api

import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// CRUD del Servidor
interface PedidoApiService {
	// R - Devuelve todos los pedidos
	@GET("pedidos")
	suspend fun getAllPedidos(): ApiResponse<List<Pedido>>

	// R - Devuelve el pedido por su id
	@GET("pedidos/{id}")
	suspend fun getByIdPedido(@Path("id") id: String): ApiResponse<Pedido>

	// C - Crea un nuevo pedido
	@POST("pedidos")
	suspend fun createPedido(@Body pedido: Pedido): ApiResponse<Pedido>

	// U - Actualización parcial de un pedido patch(update parcial) != put(update completo)
	@PATCH("pedidos/{id}")
	suspend fun updatePedido(@Path("id") id: String, @Body pedido: Pedido): ApiResponse<Pedido>

	// D - Elimina el pedido por su id
	@DELETE("pedidos/{id}")
	suspend fun deletePedido(@Path("id") id: String): ApiResponse<Any>
}