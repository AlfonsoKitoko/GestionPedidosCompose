package com.alfonsokitoko.gestionpedidos.ui.screens.add

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
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils.calcTotales
import com.alfonsokitoko.gestionpedidos.utils.PedidoValidator
import com.alfonsokitoko.gestionpedidos.utils.currencyToCentimos
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class AltaPedidoViewModel(application: Application) : AndroidViewModel(application) {
	private val altaPedidoTag = "ALTA_PEDIDO"
	private val repository = PedidoRepository(application)

	// Usamos el estado centralizado que sirve tanto para Alta como para Edit.
	var state by mutableStateOf(PedidoUiState())
		private set

	// --- VALIDACIONES ---
	val errorProveedor get() = !PedidoValidator.esProveedorValido(state.proveedor)
	val errorCantidad get() = !PedidoValidator.esEnteroValido(state.cantidad) && state.cantidad.isNotEmpty()
	val errorPrecio get() = !PedidoValidator.esDecimalValido(state.precio) && state.precio.isNotEmpty()

	val formValido
		get() = PedidoValidator.esProveedorValido(state.proveedor) &&
				PedidoValidator.esEnteroValido(state.cantidad) &&
				PedidoValidator.esDecimalValido(state.precio) &&
				state.descripcion.isNotBlank()

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
			meses = meses,
			proveedor = proveedor,
			descripcion = descripcion,
			cantidad = cantidad,
			precio = precio,
			descuento = descuento,
			fechaPedido = fechaPedido,
			numPedido = numPedido,
			fechaRecibido = fechaRecibido,
			numAlbaran = numAlbaran,
			fechaFra = fechaFra,
			vto = vto,
			numFactura = numFactura,
			totalFra = totalFra,
			cuenta = cuenta,
			mrw = mrw
		)
		// Solo recalculamos automáticamente si el usuario no ha editado el total de la factura a mano
		if (!isManualTotalFra) onInputsChanged(descuento)
	}

	private fun onInputsChanged(nuevoDesc: String = state.descuento) {

		if (state.precio.isBlank()) {
			state = state.copy(
				precioUnidad = "0,00 €",
				precioUnidadDescuento = "0,00 €",
				precioTotal = "0,00 €",
				totalFra = ""
			)
			return
		}

		val cantEfectiva = state.cantidad.ifBlank { "1" }
		val descEfectivo = nuevoDesc.ifBlank { "0" }
		// Llamada a la utilidad de cálculo centralizada
		val (unidad, unidadDesc, total) = calcTotales(
			state.precio,
			cantEfectiva,
			descEfectivo
		)

		state = state.copy(
			precioUnidad = unidad,
			precioUnidadDescuento = unidadDesc,
			precioTotal = total,
			totalFra = total
		)
		Log.d(
			altaPedidoTag,
			"Totales calculados: UnitDesc=${state.precioUnidadDescuento}, Total=${state.precioTotal}"
		)
	}

	fun guardarPedido(onSuccess: (Pedido) -> Unit, onError: () -> Unit) {
		Log.i(altaPedidoTag, "Intentando guardar pedido: ${state.proveedor}")

		viewModelScope.launch {
			try {
				// Generamos ID temporal para seguir la lógica de MongoDB
				val tempId = UUID.randomUUID().toString()

				val newPedido = Pedido(
					id = tempId,
					meses = Date(state.meses ?: System.currentTimeMillis()),
					proveedor = state.proveedor,
					descripcion = state.descripcion,
					cantidad = state.cantidad.toIntOrNull() ?: 1,
					precio = state.precio.currencyToCentimos(),
					descuento = state.descuento.replace(",", ".").toDoubleOrNull() ?: 0.0,
					precioTotal = state.precioTotal.currencyToCentimos(),
					fechaPedido = Date(state.fechaPedido ?: System.currentTimeMillis()),
					numPedido = state.numPedido,
					fechaRecibido = state.fechaRecibido?.let { Date(it) },
					numAlbaran = state.numAlbaran.ifBlank { null },
					fechaFra = state.fechaFra?.let { Date(it) },
					vto = state.vto?.let { Date(it) },
					numFactura = state.numFactura.ifBlank { null },
					totalFra = state.totalFra.currencyToCentimos(),
					cuenta = state.cuenta,
					mrw = state.mrw.ifBlank { null },
					createdAt = Date(),
					updatedAt = null,
					synced = false,
					isDeleted = false
				)

				repository.addPedido(newPedido)
				Log.i(altaPedidoTag, "Pedido guardado localmente: ${newPedido.id}")
				onSuccess(newPedido)
			} catch (e: Exception) {
				Log.e(altaPedidoTag, "Error en el guardado: ${e.message}", e)
				onError()
			}
		}
	}
}