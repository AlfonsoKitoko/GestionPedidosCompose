package com.alfonsokitoko.gestionpedidos.ui.screens.detail

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.data.repository.PedidoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailPedidoViewModel(application: Application) : AndroidViewModel(application) {
	private val repository = PedidoRepository(application)

	var showDeleteDialog by mutableStateOf(false)
		private set
	var isDeleting by mutableStateOf(false)
		private set

	fun onOpenDeleteDialog() {
		showDeleteDialog = true
	}

	fun onCloseDeleteDialog() {
		showDeleteDialog = false
	}

	fun eliminarPedido(
		pedido: Pedido,
		onBorradoExitoso: () -> Unit,
		onError: (String) -> Unit
	) {
		val id = pedido.id
		viewModelScope.launch {
			isDeleting = true
			try {
				repository.deletePedido(id)
				// Pequeño delay para que la DB local procese antes de navegar atrás
				delay(150)
				onCloseDeleteDialog()
				onBorradoExitoso()
			} catch (e: Exception) {
				Log.e("DETAIL_VM", "Error al eliminar: ${e.message}")
				onError(e.message ?: "Error desconocido")
			} finally {
				isDeleting = false
			}
		}
	}
}

/*
class DetailPedidoViewModel(application: Application) : AndroidViewModel(application) {
	private val repository = PedidoRepository(application)
	var showDeleteDialog by mutableStateOf(false)
		private set
	var isDeleting by mutableStateOf(false)
		private set

	fun onOpenDeleteDialog() {
		showDeleteDialog = true
	}

	fun onCloseDeleteDialog() {
		showDeleteDialog = false
	}

	fun eliminarPedido(
		pedido: Pedido,
		onBorradoExitoso: () -> Unit,
		onError: () -> Unit
	) {
		val id = pedido.id
		viewModelScope.launch {
			isDeleting = true
			try {
				repository.deletePedido(id)
				delay(100)
				onCloseDeleteDialog()
				onBorradoExitoso()
			} catch (e: Exception) {
				Log.e("API_LOG", "Error al eliminar: ${e.message}")
				onError()
			} finally {
				isDeleting = false
			}
		}
	}
}*/
