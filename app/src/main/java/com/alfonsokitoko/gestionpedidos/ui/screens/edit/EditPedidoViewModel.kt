package com.alfonsokitoko.gestionpedidos.ui.screens.edit

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alfonsokitoko.gestionpedidos.data.local.PedidoUiState
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.data.repository.PedidoRepository
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils
import com.alfonsokitoko.gestionpedidos.utils.PedidoValidator
import com.alfonsokitoko.gestionpedidos.utils.currencyToCentimos
import com.alfonsokitoko.gestionpedidos.utils.toUiState
import kotlinx.coroutines.launch
import java.util.Date

class EditPedidoViewModel(application: Application) : AndroidViewModel(application) {
	private val editPedidoTag = "EDIT_PEDIDO"
	private val repository = PedidoRepository(application)

	var state by mutableStateOf(PedidoUiState())
		private set

	private var pedidoId: String = ""
	private var createdAt: Date? = null

	var isSaving by mutableStateOf(false)
		private set

	/**
	 * Carga los datos del pedido en el estado de la UI
	 */
	fun init(pedido: Pedido) {
		// Guardamos los datos que no se deben perder en la edición
		pedidoId = pedido.id
		createdAt = pedido.createdAt

		// Convertimos el modelo de datos al estado de la UI
		state = pedido.toUiState()
	}

	// --- VALIDACIONES REUTILIZADAS ---
	val errorProveedor get() = !PedidoValidator.esProveedorValido(state.proveedor)
	val errorCantidad get() = !PedidoValidator.esEnteroValido(state.cantidad) && state.cantidad.isNotEmpty()
	val errorPrecio get() = !PedidoValidator.esDecimalValido(state.precio) && state.precio.isNotEmpty()
	val errorDescripcion get() = !PedidoValidator.esTextoValido(state.descripcion) && state.descripcion.isNotEmpty()

	val formValido
		get() = !errorProveedor &&
				PedidoValidator.esEnteroValido(state.cantidad) &&
				PedidoValidator.esDecimalValido(state.precio) &&
				PedidoValidator.esTextoValido(state.descripcion) &&
				state.proveedor.isNotBlank()

	/**
	 * Actualización parcial del estado desde la View
	 */
	fun onFieldChanged(
		meses: Long? = state.meses,
		proveedor: String = state.proveedor,
		descripcion: String = state.descripcion,
		cantidad: String = state.cantidad,
		precio: String = state.precio,
		descuento: String = state.descuento,
		fechaPedido: Long? = state.fechaPedido,
		numPedido: String = state.numPedido,
		fechaRecibido: Long? = state.fechaRecibido,
		numAlbaran: String = state.numAlbaran,
		fechaFra: Long? = state.fechaFra,
		vto: Long? = state.vto,
		numFactura: String = state.numFactura,
		totalFra: String = state.totalFra,
		cuenta: String = state.cuenta,
		mrw: String = state.mrw,
		isManualTotalFra: Boolean = false
	) {
		state = state.copy(
			meses = meses, proveedor = proveedor, descripcion = descripcion,
			cantidad = cantidad, precio = precio, descuento = descuento,
			fechaPedido = fechaPedido, numPedido = numPedido,
			fechaRecibido = fechaRecibido, numAlbaran = numAlbaran,
			fechaFra = fechaFra, vto = vto, numFactura = numFactura,
			totalFra = totalFra, cuenta = cuenta, mrw = mrw
		)
		if (!isManualTotalFra) recalculateTotales()
	}

	private fun recalculateTotales() {
		val cantEfectiva = state.cantidad.ifBlank { "1" }
		val (unidad, unidadDesc, total) = PedidoUtils.calcTotales(
			state.precio,
			cantEfectiva,
			state.descuento
		)

		state = state.copy(
			precioUnidad = unidad,
			precioUnidadDescuento = unidadDesc,
			precioTotal = total,
			totalFra = total // Por defecto igualamos, el usuario puede cambiarlo luego manualmente
		)
	}

	/**
	 * Ejecuta la actualización en el repositorio
	 */
	fun actualizarPedido(onSuccess: (Pedido) -> Unit, onError: (String) -> Unit) {
		if (!formValido) {
			onError("Por favor, revisa los errores en el formulario")
			return
		}

		viewModelScope.launch {
			isSaving = true
			try {
				// Reconstruimos el objeto Pedido para Room/MongoDB
				val pedidoEditado = Pedido(
					id = pedidoId, // Mantenemos el ID original
					meses = Date(state.meses ?: System.currentTimeMillis()),
					proveedor = state.proveedor,
					descripcion = state.descripcion,
					cantidad = state.cantidad.toIntOrNull() ?: 0,
					precio = state.precio.currencyToCentimos(),
					descuento = state.descuento.replace(',', '.').toDoubleOrNull() ?: 0.0,
					precioTotal = state.precioTotal.currencyToCentimos(),
					fechaPedido = Date(state.fechaPedido ?: System.currentTimeMillis()),
					numPedido = state.numPedido,
					fechaRecibido = state.fechaRecibido?.let { Date(it) },
					numAlbaran = state.numAlbaran,
					fechaFra = state.fechaFra?.let { Date(it) },
					vto = state.vto?.let { Date(it) },
					numFactura = state.numFactura,
					totalFra = state.totalFra.currencyToCentimos(),
					cuenta = state.cuenta,
					mrw = state.mrw.ifBlank { null },
					// Metadatos
					createdAt = createdAt,
					updatedAt = Date(), // Seteamos la fecha de edición actual
					synced = false,     // Forzamos resincronización
					isDeleted = false
				)

				// En tu repositorio, addPedido usa OnConflictStrategy.REPLACE
				repository.addPedido(pedidoEditado)

				Log.i(editPedidoTag, "Pedido $pedidoId actualizado localmente")
				onSuccess(pedidoEditado)
			} catch (e: Exception) {
				Log.e(editPedidoTag, "Error al actualizar: ${e.message}")
				onError(e.message ?: "Error desconocido")
			} finally {
				isSaving = false
			}
		}
	}

	/**
	 * Borrado lógico del pedido
	 */
	fun eliminarPedido(onSuccess: () -> Unit, onError: () -> Unit) {
		viewModelScope.launch {
			try {
				repository.deletePedido(pedidoId)
				onSuccess()
			} catch (e: Exception) {
				onError()
			}
		}
	}
}