package com.alfonsokitoko.gestionpedidos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.alfonsokitoko.gestionpedidos.data.model.Pedido


import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
	// --- READ ---
	// R - Obtiene todos los pedidos ordenados por fecha de creación y fecha pedido descendentes
	@Query("SELECT * FROM pedidos WHERE isDeleted=0 ORDER BY createdAt DESC,fechaPedido DESC")
	fun getAllPedidos(): Flow<List<Pedido>>

	@Query("UPDATE pedidos SET isDeleted=1, synced=0 WHERE id =:id")
	suspend fun markAsDeleted(id: String)

	@Query("SELECT * FROM pedidos WHERE isDeleted=1")
	suspend fun getPedidosPendingDelete(): List<Pedido>

	@Query("DELETE FROM pedidos WHERE id=:id")
	suspend fun deletePermanent(id: String)

	// R - Obtiene un pedido por su id. Devuelve null si o existe
	@Query("SELECT * FROM pedidos WHERE id=:id LIMIT 1")
	suspend fun getByIdPedido(id: String): Pedido?

	// --- CREATE / UPDATE ---
	// C/U - Si ya existe pedido con su mismo id, lo reemplaza
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPedido(pedido: Pedido)

	// C - Inserta lista de pedidos en la db
	// Si existen conflictos de id, son reemplazados
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAllPedidos(pedidos: List<Pedido>)

	// --- DELETE ---

	// D - Elimina un pedido de la db por su id
	@Query("DELETE FROM pedidos WHERE id=:id")
	suspend fun deletePedidoById(id: String)

	// D - Elimina todos los pedidos
	@Query("DELETE FROM pedidos")
	suspend fun deleteAllPedidos()

	@Query("SELECT * FROM pedidos WHERE synced = 0")
	suspend fun getPedidosUnsynced(): List<Pedido>

	@Query("UPDATE pedidos SET synced = 1 WHERE id = :id")
	suspend fun updateSynced(id: String)
}
