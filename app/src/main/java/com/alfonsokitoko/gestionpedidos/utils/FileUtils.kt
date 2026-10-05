package com.alfonsokitoko.gestionpedidos.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

fun generateCSV(context: Context, pedidos: List<Pedido>): File? {
	val filename = "export_pedidos_${System.currentTimeMillis()}.csv"
	val utf8BOM = "\uFEFF"

	val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
	val sdfMes = SimpleDateFormat("MM/yyyy", Locale.getDefault())

	val csvHeader =
		"MESES;PROVEEDOR;DESCRIPCIÓN DEL PRODUCTO;CANTIDAD;PRECIO;FECHA PEDIDO;Nº PEDIDO;FECHA RECIBIDO;Nº ALBARÁN;DTO.;PRECIO UNIDAD;PRECIO UNIDAD CON DTO.;PRECIO TOTAL;FECHA FRA.;VTO.;Nº FACTURA;TOTAL FRA.;CUENTA;MRW\n"

	fun Long?.toExcelValue(): String =
		(this ?: 0L).toCurrency().replace("€", "").replace(Regex("[\\s\\u00A0]"), "").trim()

	return try {
		val csvContent = StringBuilder().apply {
			append(utf8BOM)
			append(csvHeader)

			pedidos.forEach { p ->
				val numPedidoClean = PedidoUtils.obtenerNumeroPedidoLimpio(p).replace(";", " ")

				val (_, precioUnidadDto, _) = PedidoUtils.calcTotales(
					p.precio.toCurrency(),
					p.cantidad.toString(),
					p.descuento.toString()
				)

				append("${p.meses.let { sdfMes.format(it) }};")        // MESES
				append("${p.proveedor.replace(";", " ")};")            // PROVEEDOR
				append("${p.descripcion.replace(";", " ").replace("\n", " ")};") // DESCRIPCIÓN
				append("${p.cantidad};")                               // CANTIDAD
				append("${p.precio.toExcelValue()};")                  // PRECIO
				append("${sdfDia.format(p.fechaPedido)};")             // FECHA PEDIDO
				append("$numPedidoClean;")                            // Nº PEDIDO
				append("${p.fechaRecibido?.let { sdfDia.format(it) } ?: ""};") // FECHA RECIBIDO
				append("${p.numAlbaran?.replace(";", " ") ?: ""};")    // Nº ALBARÁN
				append("${p.descuento.toString().replace(".", ",")};") // DTO.

				// Reutilizamos los valores formateados
				append("${p.precio.toExcelValue()};")                  // PRECIO UNIDAD
				append("${precioUnidadDto.replace("€", "").trim()};")  // PRECIO UNIDAD CON DTO.
				append("${p.precioTotal.toExcelValue()};")             // PRECIO TOTAL

				append("${p.fechaFra?.let { sdfDia.format(it) } ?: ""};") // FECHA FRA.
				append("${p.vto?.let { sdfDia.format(it) } ?: ""};")      // VTO.
				append("${p.numFactura?.replace(";", " ") ?: ""};")       // Nº FACTURA
				append("${p.totalFra.toExcelValue()};")                   // TOTAL FRA.
				append("${p.cuenta.replace(";", " ")};")                  // CUENTA
				append("${p.mrw?.replace(";", " ") ?: ""}\n")             // MRW
			}
		}.toString()

		val file = File(context.cacheDir, filename)
		file.writeText(csvContent, Charsets.UTF_8)
		file
	} catch (e: Exception) {
		Log.e("EXPORT_CSV", "Error al generar el archivo: ${e.message}")
		null
	}
}

fun enviarArchivo(context: Context, file: File): Intent {
	val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
	return Intent(Intent.ACTION_SEND).apply {
		type = "text/csv"
		putExtra(Intent.EXTRA_STREAM, uri)
		addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	}
}