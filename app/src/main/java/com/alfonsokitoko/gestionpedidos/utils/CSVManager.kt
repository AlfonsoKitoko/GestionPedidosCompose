package com.alfonsokitoko.gestionpedidos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CSVManager(private val context: Context) {

	fun generarCsvNuevos(uriExcelOg: Uri, pedidosApp: List<Pedido>) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				val inStream = context.contentResolver.openInputStream(uriExcelOg)
				val workbook = HSSFWorkbook(inStream)

				// 1. Seleccionamos la hoja PENDIENTES
				val hoja = workbook.getSheet("PENDIENTES") ?: workbook.getSheetAt(1)
				val dataFormatter = DataFormatter()

				// 2. Limpieza Nuclear para comparación
				fun normalizar(t: String): String {
					return t.uppercase()
						.replace("- ANULADOS", "")
						.replace("-ANULADOS", "")
						.replace(Regex("[^A-Z0-9]"), "")
						.trim()
				}

				val clavesExistentes = mutableSetOf<String>()

				for (i in 0..hoja.lastRowNum) {
					val fila = hoja.getRow(i) ?: continue
					val provRaw = dataFormatter.formatCellValue(fila.getCell(1))
					val descRaw = dataFormatter.formatCellValue(fila.getCell(2))
					val cantRaw = dataFormatter.formatCellValue(fila.getCell(3))

					if (provRaw.isNullOrBlank() || provRaw.uppercase().contains("PROVEEDOR")) continue
					if (descRaw.isNullOrBlank()) continue

					val prov = normalizar(provRaw)
					val desc = normalizar(descRaw)
					val cant = cantRaw.split(Regex("[.,]"))[0].trim()

					clavesExistentes.add(prov + desc + cant)
				}

				workbook.close()
				inStream?.close()

				// 3. Filtrado de Novedades
				val pedidosNuevos = pedidosApp.filter { p ->
					val provP = normalizar(p.proveedor)
					val descP = normalizar(p.descripcion)
					val cantP = p.cantidad.toString().split(".")[0].trim()
					val claveApp = provP + descP + cantP
					!clavesExistentes.contains(claveApp)
				}

				withContext(Dispatchers.Main) {
					if (pedidosNuevos.isEmpty()) {
						Toast.makeText(context, "Hoja PENDIENTES al día.", Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(context, "¡Detectados ${pedidosNuevos.size} nuevos!", Toast.LENGTH_LONG)
							.show()
					}
				}

				if (pedidosNuevos.isEmpty()) return@launch
				kotlinx.coroutines.delay(600)

				// 4. Construcción del CSV con formato Excel Español
				val csvContent = StringBuilder()
				csvContent.append("MESES;PROVEEDOR;DESCRIPCIÓN DEL PRODUCTO;CANTIDAD;PRECIO;FECHA PEDIDO;Nº PEDIDO;FECHA RECIBIDO;Nº ALBARÁN;DTO.;PRECIO UNIDAD;PRECIO UNIDAD CON DTO.;PRECIO TOTAL;FECHA FRA.;VTO.;Nº FACTURA;TOTAL FRA.;CUENTA;MRW\n")

				val sdfD = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
				val sdfM = SimpleDateFormat("MM/yyyy", Locale.getDefault())

				pedidosNuevos.forEach { p ->
					val numPedido = PedidoUtils.obtenerNumeroPedidoLimpio(p)
					val (_, precioUnidadDto, _) = PedidoUtils.calcTotales(
						p.precio.toCurrency(), p.cantidad.toString(), p.descuento.toString()
					)

					// Formateo de moneda (sin puntos de miles, coma para decimales)
					fun Long.toCsvV(): String = this.toCurrency()
						.replace("€", "")
						.replace(".", "")
						.trim()

					// Ajuste de Descuento (Si es 0.5 -> 50, si es 10.0 -> 10)
					val dtoFormateado = if (p.descuento > 0 && p.descuento < 1) {
						(p.descuento * 100).toString()
					} else {
						p.descuento.toString()
					}.replace(".0", "").replace(".", ",")

					csvContent.append("${p.meses?.let { sdfM.format(it) } ?: ""};")
					csvContent.append("${p.proveedor.uppercase()};")
					csvContent.append("${p.descripcion};")
					csvContent.append("${p.cantidad};")
					csvContent.append("${p.precio.toCsvV().replace(",", ".")};") // Cambiamos a coma abajo
					csvContent.append("${p.fechaPedido?.let { sdfD.format(it) } ?: ""};")
					csvContent.append("$numPedido;")
					csvContent.append("${p.fechaRecibido?.let { sdfD.format(it) } ?: ""};")
					csvContent.append("${p.numAlbaran ?: ""};")
					csvContent.append("$dtoFormateado;")
					csvContent.append("${p.precio.toCsvV().replace(",", ".")};")
					csvContent.append(
						"${
							precioUnidadDto.replace("€", "").trim().replace(".", "").replace(",", ".")
						};"
					)
					csvContent.append("${p.precioTotal.toCsvV().replace(",", ".")};")
					csvContent.append("${p.fechaFra?.let { sdfD.format(it) } ?: ""};")
					csvContent.append("${p.vto?.let { sdfD.format(it) } ?: ""};")
					csvContent.append("${p.numFactura ?: ""};")
					csvContent.append("${p.totalFra?.toCsvV()?.replace(",", ".")};")
					csvContent.append("${p.cuenta};")
					csvContent.append("${p.mrw ?: ""}\n")
				}

				// 5. Escritura con BOM para corregir tildes en Excel Windows
				val humanDate = SimpleDateFormat("dd_MM_yyyy_HHmm", Locale.getDefault()).format(Date())
				val archivoCsv = File(context.cacheDir, "PEDIDOS_NUEVOS_$humanDate.csv")

				val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
				archivoCsv.writeBytes(bom + csvContent.toString().toByteArray(Charsets.UTF_8))

				withContext(Dispatchers.Main) {
					enviarArchivo(archivoCsv)
				}
			} catch (e: Exception) {
				Log.e("CSV_ERROR", "Error: ${e.message}")
			}
		}
	}

	private fun enviarArchivo(file: File) {
		val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/comma-separated-values"
			putExtra(Intent.EXTRA_EMAIL, arrayOf("alakazam951@gmail.com"))
			putExtra(Intent.EXTRA_SUBJECT, "Registro de pedidos nuevos (PENDIENTES)")
			putExtra(Intent.EXTRA_TEXT, "Añade estas líneas al final de tu Excel maestro.")
			putExtra(Intent.EXTRA_STREAM, uri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		context.startActivity(Intent.createChooser(intent, "Enviar email"))
	}
}