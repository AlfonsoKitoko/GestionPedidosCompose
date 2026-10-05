package com.alfonsokitoko.gestionpedidos.utils

import androidx.compose.ui.graphics.Color
import com.alfonsokitoko.gestionpedidos.data.local.PedidoUiState
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToLong

private val localeSpain = Locale.forLanguageTag("es-ES")

fun Long.toCurrency(): String {
	val euros = this / 100.0
	val formatter = NumberFormat.getCurrencyInstance(localeSpain).apply {
		// Siempre mostrará dos decimales
		minimumFractionDigits = 2
		maximumFractionDigits = 2
	}

	return formatter.format(euros)
}

fun String.currencyToCentimos(): Long {
	if (this.isBlank()) return 0L

	return try {
		val limpio = this.replace(Regex("[^0-9,]"), "")
		val normalizado = limpio.replace(",", ".")
		val doubleValue = normalizado.toDoubleOrNull() ?: 0.0
		(doubleValue * 100).roundToLong()
	} catch (_: Exception) {
		0L
	}
	/*return try {
		val limpio = this
			.replace("€", "")
			.replace("$", "")
			.replace(Regex("[\\s\\u00A0]"), "")  // Quita espacios normales y no rotura
			.trim()
		val normalizado = if (limpio.contains(",") && limpio.contains(".")) {
			limpio
				.replace(".", "")
				.replace(",", ".")
		} else {
			limpio.replace(",", ".")
		}
		val doubleValue = normalizado.toDoubleOrNull() ?: 0.0

		(doubleValue * 100).roundToLong()
	} catch (e: Exception) {
		0L
	}*/
}

// COMPRAS GENERICO [AZUL: DOCUMENTADO (FRA.) // ROJO: PTO. VENIR MATERIAL // VERDE: RECIBIDO MATERIAL(ALB.)
object PedidoUtils {
	// AZUL: DOCUMENTADO (FRA.)
	val azulDocumentado = Color(0xFF2196F3)

	// VERDE: RECIBIDO MATERIAL(ALB.)
	val verdeRecibido = Color(0xFF4CAF50)

	// ROJO: PTO. VENIR MATERIAL //
	val rojoPtoVenir = Color(0xFFF44336)

	fun obtenerColorEstado(pedido: Pedido): Color {
		return when (pedido.estado?.uppercase()) {
			"AZUL" -> azulDocumentado
			"VERDE" -> verdeRecibido
			"ROJO" -> rojoPtoVenir

			else -> {
				when {
					!pedido.numFactura.isNullOrBlank() || pedido.fechaFra != null -> azulDocumentado
					!pedido.numAlbaran.isNullOrBlank() || pedido.fechaRecibido != null -> verdeRecibido
					else -> rojoPtoVenir
				}
			}
		}
	}

	fun obtenerTextoEstado(pedido: Pedido): String {
		return when (obtenerColorEstado(pedido)) {
			azulDocumentado -> "DOCUMENTADO (FRA)"
			verdeRecibido -> "RECIBIDO (ALB)"
			else -> "PTO. VENIR MATERIAL"
		}
	}

	fun obtenerNumeroPedidoLimpio(pedido: Pedido): String {
		val num = pedido.numPedido
		if (num.isNullOrBlank()) return "Sin número"

		val esFechaBasura = num.contains("00:00:00") ||
				num.contains("GMT") ||
				num.contains("CET")

		return if (esFechaBasura) {
			val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
			fmt.format(pedido.fechaPedido)
		} else {
			num
		}
	}

	fun calcTotales(
		precioInput: String,
		cantidadInput: String,
		descuentoInput: String
	): Triple<String, String, String> {
		val pUnidadCentimos = precioInput.currencyToCentimos()
		val cant = cantidadInput.toIntOrNull() ?: 1
		val desc = descuentoInput.replace(",", ".").toDoubleOrNull() ?: 0.0

		val factorDescuento = (1.0 - desc / 100.0)

		val precioUnidadDescuento = (pUnidadCentimos * factorDescuento).roundToLong()
		val precioTotal = precioUnidadDescuento * cant

		return Triple(
			pUnidadCentimos.toCurrency(),
			precioUnidadDescuento.toCurrency(),
			precioTotal.toCurrency(),
		)
	}
}

fun Pedido.toUiState(): PedidoUiState {
	return PedidoUiState(
		meses = this.meses.time,
		proveedor = this.proveedor,
		descripcion = this.descripcion,
		cantidad = this.cantidad.toString(),
		precio = String.format(localeSpain, "%.2f", this.precio / 100.0),
		descuento = this.descuento.toString(),
		fechaPedido = this.fechaPedido.time,
		numPedido = this.numPedido ?: "",
		fechaRecibido = this.fechaRecibido?.time,
		numAlbaran = this.numAlbaran ?: "",
		fechaFra = this.fechaFra?.time,
		vto = this.vto?.time,
		numFactura = this.numFactura ?: "",
		totalFra = String.format(localeSpain, "%.2f", (this.totalFra ?: 0L) / 100.0),
		cuenta = this.cuenta,
		mrw = this.mrw ?: "",
		precioUnidad = this.precio.toCurrency(),
		precioUnidadDescuento = this.precioUnidadDescuentoCalculado ?: "0,00 €", // Si viene de Mongo
		precioTotal = this.precioTotal.toCurrency()
	)
}