package com.alfonsokitoko.gestionpedidos.data.repository

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.alfonsokitoko.gestionpedidos.data.api.RetrofitClient
import com.alfonsokitoko.gestionpedidos.data.local.AppDatabase
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Intermediario de los datos entre servidor (Retrofit/MongoDB) y local (Room)
class PedidoRepository(private val context: Context) {
	private val apiTag = "API_SYNC"
	private val daoTag = "DAO_LOCAL"

	// Cliente API para acceso a la Base de datos en Servidor (MongoDB)
	private val api = RetrofitClient.getPedidoService(context)

	// Acceso a la persistencia local (Room)
	private val dao = AppDatabase.getDatabase(context).pedidoDao()

	// Emite la lista de pedidos actualizada en tiempo real directamente desde Room
	val allPedidos: Flow<List<Pedido>> = dao.getAllPedidos()

	// Sincroniza la base de datos local con la del servidor
	// Descarga los pedidos de MongoDb e invalida /actualiza Room con los datos
	// En PedidoRepository.kt

	suspend fun refreshPedidos() {
		withContext(Dispatchers.IO) {
			try {
				val response = api.getAllPedidos()
				if (response.success) {
					val pedidosServer = response.data ?: emptyList()

					// 1. Obtenemos los que están en Room esperando a ser subidos
					val localesPendientes = dao.getPedidosUnsynced()

					// 2. Saneamos antes de insertar
					pedidosServer.forEach { pServer ->
						val coincidencia = localesPendientes.find { it.esMismoNegocioQue(pServer) }

						if (coincidencia != null && coincidencia.id != pServer.id) {
							// Si encontramos uno igual pero con ID diferente (el temporal),
							// borramos el temporal de Room.
							dao.deletePedidoById(coincidencia.id)
							Log.d(daoTag, "Saneando duplicado: Borrado temporal ${coincidencia.id}")
						}
					}

					// 3. Insertamos los datos frescos del servidor
					val idsMarcados = dao.getPedidosPendingDelete().map { it.id }.toSet()
					val pedidosFiltrados = pedidosServer
						.filter { it.id !in idsMarcados }
						.map { it.copy(synced = true) }

					dao.insertAllPedidos(pedidosFiltrados)
				}
			} catch (e: Exception) {
				Log.e(daoTag, "Error en refresh: ${e.message}")
				throw e
			}
		}
	}

	// Obtiene la lista directamente a través del Servidor sin pasar por Room
	suspend fun getAllPedidos(): List<Pedido> {
		return withContext(Dispatchers.IO) {
			try {
				val response = api.getAllPedidos()
				if (response.success) response.data ?: emptyList()
				else emptyList()
			} catch (e: Exception) {
				e.printStackTrace()
				emptyList()
			}
		}
	}

	// Busca pedido por su id en servidor
	suspend fun getByIdPedido(id: String): Pedido? {
		return withContext(Dispatchers.IO) {
			try {
				val response = api.getByIdPedido(id)
				if (response.success) response.data else dao.getByIdPedido(id)
			} catch (e: Exception) {
				dao.getByIdPedido(id)
			}
		}
	}

	// Envía el nuevo pedido al servidor, si es exitoso, se persiste también en Room
	suspend fun addPedido(newPedido: Pedido) {
		return withContext(Dispatchers.IO) {
			dao.insertPedido(newPedido.copy(synced = false))

			val exito = syncPedidoReal(newPedido)
			Log.d(daoTag, "Pedido ${newPedido.id} guardado en Room. Lanzando SyncWorker...")

			if (!exito) enqueueSyncWork()
		}
	}

	suspend fun syncPedidoReal(pedidoPendiente: Pedido): Boolean {
		return try {
			val esNuevo = pedidoPendiente.id.length != 24

			if (esNuevo) {
				val response = api.createPedido(pedidoPendiente.copy(id = ""))

				if (response.success && response.data != null) {
					val pedidoServer = response.data
					dao.deletePedidoById(pedidoPendiente.id)
					dao.insertPedido(response.data.copy(synced = true))
					true
				} else false

			} else {
				val result = updatePedidoInterno(pedidoPendiente.id, pedidoPendiente)
				result != null
			}
		} catch (e: Exception) {
			Log.e(apiTag, "Excepción (SYNC REAL): ${e.message}")
			false
		}
	}

	suspend fun updatePedidoInterno(id: String, pedido: Pedido): Pedido? {
		return withContext(Dispatchers.IO) {
			try {
				dao.insertPedido(pedido.copy(synced = false))

				val response = api.updatePedido(id, pedido)

				if (response.success && response.data != null) {
					val updatedFromServer = response.data
					dao.insertPedido(updatedFromServer.copy(synced = true))
					updatedFromServer
				} else {
					Log.w(apiTag, "Fallo en PATCH(suyccess=false). Retry Worker")
					enqueueSyncWork()
					pedido
				}
			} catch (e: Exception) {
				Log.e(apiTag, "Excepción (UPDATE): ${e.message}")
				enqueueSyncWork()
				pedido
			}
		}
	}

	suspend fun deletePedido(id: String) {
		withContext(Dispatchers.IO) {
			dao.markAsDeleted(id)
			Log.d(daoTag, "Pedido marcado para borrar: $id")
			enqueueSyncWork()
		}
	}

	private fun Pedido.esMismoNegocioQue(otro: Pedido): Boolean {
		// 1. Datos críticos que NO deberían cambiar
		val mismaFechaPedido = this.meses.time == otro.meses.time
		val mismoProveedor = this.proveedor.trim().equals(otro.proveedor.trim(), ignoreCase = true)
		val mismoPrecio =
			abs(this.precioTotal - otro.precioTotal) < 0.01 // Evita errores de redondeo
		val mismaCantidad = this.cantidad == otro.cantidad

		// 2. Si tiene número de pedido, es el mejor identificador
		if (!this.numPedido.isNullOrBlank() && !otro.numPedido.isNullOrBlank()) {
			return mismoProveedor && this.numPedido == otro.numPedido
		}

		// 3. Si no hay número de pedido, usamos la combinación de datos + margen de tiempo
		// Si se crearon el mismo día (86400000 ms), es muy sospechoso
		val tiempoEste = this.createdAt?.time ?: 0L
		val tiempoOtro = otro.createdAt?.time ?: 0L
		val margenCreacion = abs(tiempoEste - tiempoOtro) < 86400000

		return mismoProveedor && mismaFechaPedido && mismoPrecio && mismaCantidad && margenCreacion
	}

	fun enqueueSyncWork() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
			.setConstraints(constraints)
			.addTag("SYNC_WORKER")
			.setBackoffCriteria(
				BackoffPolicy.LINEAR,
				WorkRequest.MIN_BACKOFF_MILLIS,
				TimeUnit.MILLISECONDS
			)
			.build()

		WorkManager.getInstance(context).enqueueUniqueWork(
			"pedido_sync_unique",
			ExistingWorkPolicy.KEEP,
			syncRequest
		)
	}
}