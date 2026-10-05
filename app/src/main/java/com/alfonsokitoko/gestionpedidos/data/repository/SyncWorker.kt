package com.alfonsokitoko.gestionpedidos.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alfonsokitoko.gestionpedidos.data.api.RetrofitClient
import com.alfonsokitoko.gestionpedidos.data.local.AppDatabase

// TODO: arreglar el sincronismo entre local y backend DONE
class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
	CoroutineWorker(appContext, workerParams) {

	private val syncTag = "SYNC_WORKER"

	override suspend fun doWork(): Result {
		if (runAttemptCount > 3) {
			Log.e(syncTag, "Demasiados reintentos. Abortando sincronización.")
			return Result.failure()
		}
		Log.i(syncTag, "[INICIO SYNC ${System.currentTimeMillis()}]")

		return try {
			val api = RetrofitClient.getPedidoService(applicationContext)
			val repo = PedidoRepository(applicationContext)
			val db = AppDatabase.getDatabase(applicationContext)
			val dao = db.pedidoDao()
			var allSuccess = true

			Log.i(syncTag, "Intentando sincronizar pedidos")


			val paraBorrar = dao.getPedidosPendingDelete()
			paraBorrar.forEach { pedido ->
				try {
					val response = api.deletePedido(pedido.id)

					if (response.success) {
						dao.deletePermanent(pedido.id)
						Log.d(
							syncTag,
							"Pedido ${pedido.id} (numPed:${pedido.numPedido} ,prov:${pedido.proveedor}) borrado con éxito"
						)
					} else {
						Log.w(
							syncTag,
							"Fallo al borrar pedido (${pedido.id}) cód:${response.message}"
						)
						allSuccess = false
					}
				} catch (e: Exception) {
					Log.e(syncTag, "Error al borrar pedido ${pedido.id}", e)
					allSuccess = false
				}
			}

			val pendientes = dao.getPedidosUnsynced()
			val paraSync = pendientes.filter { !it.isDeleted }

			if (paraSync.isNotEmpty()) {
				Log.d(syncTag, "Encontrados ${paraSync.size} pedidos pendientes de subir/actualizar")
				showSyncNotification(applicationContext, "Sincronizando ${paraSync.size} pedidos...")

				paraSync.forEach { pedido ->
					try {
						Log.v(syncTag, "Sincronizando pedido: ${pedido.id} (${pedido.proveedor})")
						val result = repo.syncPedidoReal(pedido)

						if (result) {
							Log.d(syncTag, "Sincronización exitosa: ${pedido.id}")
						} else {
							Log.w(syncTag, "Error en syncPedidoReal para ID: ${pedido.id}")
							allSuccess = false
						}
					} catch (e: Exception) {
						Log.e(syncTag, "Fallo crítico sincronizando ID: ${pedido.id}", e)
						allSuccess = false
					}
				}
				val notificationManager =
					applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
				notificationManager.cancel(1)
			}

			if (allSuccess) {
				Log.i(syncTag, "Sincronización completa")
				val totalBorrados = paraBorrar.size
				val totalSincronizados = paraSync.size
				Log.i(
					syncTag,
					"Resumen Sync: Borrados $totalBorrados, Sincronizados $totalSincronizados. Éxito: $allSuccess"
				)
				Result.success()
			} else {
				Log.w(syncTag, "Sincronización fallida")
				Result.retry()
			}
		} catch (e: Exception) {
			Log.e(syncTag, "FATAL ERROR en el Sync Worker (intento $runAttemptCount): ${e.message}", e)
			Result.retry()
		}
	}

	private fun showSyncNotification(context: Context, message: String) {
		val channelId = "sync_channel"
		val notificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel =
				NotificationChannel(channelId, "Sincronización", NotificationManager.IMPORTANCE_LOW)
			notificationManager.createNotificationChannel(channel)
		}
		val notification = NotificationCompat.Builder(context, channelId)
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setContentTitle("Sincronizando pedidos...")
			.setContentText(message)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setOngoing(true)
			.build()

		notificationManager.notify(1, notification)
	}
}