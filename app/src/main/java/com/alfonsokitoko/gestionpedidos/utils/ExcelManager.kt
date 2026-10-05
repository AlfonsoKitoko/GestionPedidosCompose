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
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FillPatternType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelManager(private val context: Context) {

	fun injectPendientes(uriOriginal: Uri, pedidosNuevos: List<Pedido>) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				val inStream = context.contentResolver.openInputStream(uriOriginal)
				val workbook = HSSFWorkbook(inStream)
				inStream?.close()

				val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
				val sdfMes = SimpleDateFormat("MM/yyyy", Locale.getDefault())

				fun Long?.toExcelValue(): String =
					(this ?: 0L).toCurrency().replace("€", "").replace(Regex("[\\s\\u00A0]"), "").trim()

				// 1. OBTENER HOJA Y ESTILOS
				val hojaPendientes = workbook.getSheet("PENDIENTES") ?: workbook.getSheetAt(1)
				val estilos = configColoresCustom(workbook)

				// 1. ESCANEO DE FILAS (El que ya nos funciona)
				var ultimaFilaConTexto = -1
				var contadorVaciosSeguidos = 0
				val dataFormatter = DataFormatter() // Para leer EXACTO lo que ves en Excel

				for (i in 0..5000) {
					val f = hojaPendientes.getRow(i)
					val prov = dataFormatter.formatCellValue(f?.getCell(1)).trim()
					val desc = dataFormatter.formatCellValue(f?.getCell(2)).trim()

					if (prov.isEmpty() && desc.isEmpty()) {
						contadorVaciosSeguidos++
					} else {
						ultimaFilaConTexto = i
						contadorVaciosSeguidos = 0
					}
					if (contadorVaciosSeguidos >= 20) break
				}
				var filaDondeEmpezar = ultimaFilaConTexto + 1

				// 2. LEER EXISTENTES (Con chivato de Log)
				val clavesExistentes = mutableSetOf<String>()
				for (i in 2..ultimaFilaConTexto) {
					val fila = hojaPendientes.getRow(i) ?: continue
					val prov = dataFormatter.formatCellValue(fila.getCell(1)).trim().lowercase()
					val desc = dataFormatter.formatCellValue(fila.getCell(2)).trim().lowercase()
					val cant = dataFormatter.formatCellValue(fila.getCell(3)).split(".")[0].trim().lowercase()
					val idC6 = dataFormatter.formatCellValue(fila.getCell(6)).trim().lowercase()

					val clave =
						if (idC6.isNotEmpty() && idC6 != "nº pedido" && idC6 != "nºpedido" && idC6 != "mensual") {
							idC6
						} else {
							// Si no hay ID real, generamos la misma clave compuesta que en la App
							val prov = dataFormatter.formatCellValue(fila.getCell(1)).trim().lowercase()
							val desc = dataFormatter.formatCellValue(fila.getCell(2)).trim().lowercase()
							val cant =
								dataFormatter.formatCellValue(fila.getCell(3)).split(Regex("[.,]"))[0].trim()
							"${prov}_${desc}_$cant".replace(Regex("\\s+"), "")
						}

					if (clave.isNotEmpty()) {
						clavesExistentes.add(clave)
						if (i < 5) Log.d("ExcelManager", "DEBUG EXCEL (Fila $i): Clave generada -> $clave")
					}
				}

// 3. FILTRAR NUEVOS (Con chivato de Log)
				var logsImpresos = 0
				val pedidosAInyectar = pedidosNuevos.filter { p ->
					val claveApp = generarClaveUnica(p)
					val yaExiste = clavesExistentes.contains(claveApp)

					if (!yaExiste && logsImpresos < 5) {
						Log.d(
							"ExcelManager",
							"DEBUG APP (Pedido ${p.proveedor}): Clave generada -> $claveApp | ¿Existe en Excel? $yaExiste"
						)
						logsImpresos++
					}
					!yaExiste
				}

				Log.d(
					"ExcelManager",
					"FINAL -> Filas en Excel: $ultimaFilaConTexto | Pedidos App: ${pedidosNuevos.size} | NUEVOS A INYECTAR: ${pedidosAInyectar.size}"
				)
				// 5. INYECCIÓN
				pedidosAInyectar.forEach { p ->
					val fila = hojaPendientes.createRow(filaDondeEmpezar++)

					val numPedidoClean = PedidoUtils.obtenerNumeroPedidoLimpio(p)
					val (_, precioUnidadDto, _) = PedidoUtils.calcTotales(
						p.precio.toCurrency(),
						p.cantidad.toString(),
						p.descuento.toString()
					)

					fila.createCell(0).setCellValue(p.meses?.let { sdfMes.format(it) } ?: "")
					fila.createCell(1).setCellValue(p.proveedor)
					fila.createCell(2).setCellValue(p.descripcion)
					fila.createCell(3).setCellValue(p.cantidad.toDouble())
					fila.createCell(4).setCellValue(p.precio.toExcelValue())
					fila.createCell(5).setCellValue(p.fechaPedido?.let { sdfDia.format(it) } ?: "")
					fila.createCell(6).setCellValue(numPedidoClean)
					fila.createCell(7).setCellValue(p.fechaRecibido?.let { sdfDia.format(it) } ?: "")
					fila.createCell(8).setCellValue(p.numAlbaran ?: "")
					fila.createCell(9).setCellValue(p.descuento.toString().replace(".", ","))
					fila.createCell(10).setCellValue(p.precio.toExcelValue())
					fila.createCell(11).setCellValue(precioUnidadDto.replace("€", "").trim())
					fila.createCell(12).setCellValue(p.precioTotal.toExcelValue())
					fila.createCell(13).setCellValue(p.fechaFra?.let { sdfDia.format(it) } ?: "")
					fila.createCell(14).setCellValue(p.vto?.let { sdfDia.format(it) } ?: "")
					fila.createCell(15).setCellValue(p.numFactura ?: "")
					fila.createCell(16).setCellValue(p.totalFra.toExcelValue())
					fila.createCell(17).setCellValue(p.cuenta)
					fila.createCell(18).setCellValue(p.mrw ?: "")

					val estiloElegido = determinarEstilo(p, estilos)
					if (estiloElegido != null) {
						for (i in 0..18) {
							val celda = fila.getCell(i) ?: fila.createCell(i)
							celda.cellStyle = estiloElegido as HSSFCellStyle
						}
					}
				}

				// 5. GUARDADO Y CIERRE (En orden correcto)
				val fechaHuma = SimpleDateFormat("dd_MM_yyyy_HHmm", Locale.getDefault()).format(Date())
				val nomOut = "ACTUALIZACION_$fechaHuma.xls"
				val archivoFinal = File(context.cacheDir, nomOut)

				val outStream = FileOutputStream(archivoFinal)
				workbook.write(outStream)
				outStream.flush()
				outStream.close()
				workbook.close()

				withContext(Dispatchers.Main) {
					if (pedidosAInyectar.isEmpty()) {
						Toast.makeText(context, "No hay pedidos nuevos que añadir", Toast.LENGTH_SHORT).show()
					} else {
						val intentEnvio = enviarArchivo(context, archivoFinal)
						val chooser = Intent.createChooser(intentEnvio, "Enviar Excel actualizado")
						chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						context.startActivity(chooser)
					}
				}

			} catch (e: Exception) {
				e.printStackTrace()
				withContext(Dispatchers.Main) {
					Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun generarClaveUnica(p: Pedido): String {
		val numP = PedidoUtils.obtenerNumeroPedidoLimpio(p).trim().lowercase()

		// Si el número es "sin número", vacío, o "mensual", usamos la clave compuesta
		return if (numP.isNotEmpty() && numP != "sin número" && numP != "mensual") {
			numP
		} else {
			val prov = p.proveedor.trim().lowercase()
			val desc = p.descripcion.trim().lowercase()
			val cant = p.cantidad.toString().split(".")[0].trim()
			"${prov}_${desc}_$cant".replace(Regex("\\s+"), "")
		}
	}

	private fun determinarEstilo(p: Pedido, estilos: Map<String, CellStyle>): CellStyle? {
		return when {
			p.descripcion.contains("ANULADO", ignoreCase = true) -> estilos["AMARILLO"]
			p.descripcion.contains("pto. poner", ignoreCase = true) -> estilos["ROJO"]
			p.numAlbaran?.isNotEmpty() == true || p.fechaRecibido != null -> estilos["VERDE"]
			p.numFactura?.isNotEmpty() == true -> estilos["AZUL"]
			else -> null
		}
	}

	private fun configColoresCustom(workbook: HSSFWorkbook): Map<String, CellStyle> {
		val palette = workbook.customPalette
		val estilos = mutableMapOf<String, CellStyle>()

		fun registrarColor(id: Short, r: Int, g: Int, b: Int, nombre: String) {
			palette.setColorAtIndex(id, r.toByte(), g.toByte(), b.toByte())
			val style = workbook.createCellStyle().apply {
				fillForegroundColor = id
				fillPattern = FillPatternType.SOLID_FOREGROUND
				borderTop = BorderStyle.THIN
				borderLeft = BorderStyle.THIN
				borderBottom = BorderStyle.THIN
				borderRight = BorderStyle.THIN
			}
			estilos[nombre] = style
		}

		registrarColor(40, 255, 255, 0, "AMARILLO")
		registrarColor(41, 255, 124, 128, "ROJO")
		registrarColor(42, 216, 228, 188, "VERDE")
		registrarColor(43, 184, 204, 228, "AZUL")

		return estilos
	}

	private fun enviarArchivo(context: Context, file: File): Intent {
		val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
		return Intent(Intent.ACTION_SEND).apply {
			type = "application/vnd.ms-excel"
			putExtra(Intent.EXTRA_STREAM, uri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
	}

	fun enviarExcelPorEmail(context: Context, excelFile: File) {

		val uri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.fileprovider",
			excelFile
		)

		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
			putExtra(Intent.EXTRA_EMAIL, arrayOf("usuario@generico.com"))
			putExtra(Intent.EXTRA_SUBJECT, "Registro de entrega de EPIs")
			putExtra(
				Intent.EXTRA_TEXT,
				"Se adjunta el registro actualizado de entrega de EPIs ."
			)
			putExtra(Intent.EXTRA_STREAM, uri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}

		context.startActivity(
			Intent.createChooser(intent, "Enviar email")
		)
	}
}