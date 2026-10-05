package com.alfonsokitoko.gestionpedidos.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.ui.components.BaseLayout
import com.alfonsokitoko.gestionpedidos.utils.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPedidoScreen(
	pedido: Pedido,
	viewModel: DetailPedidoViewModel = viewModel(),
	onVolver: () -> Unit,
	onEditarClick: (Pedido) -> Unit,
	onBorradoExitoso: (Pedido) -> Unit
) {
	val context = LocalContext.current
	val esAzul = pedido.estado == "AZUL"
	var showUnlockDialog by remember { mutableStateOf(false) }

	val fmtMes = remember { java.text.SimpleDateFormat("MMM yy", java.util.Locale.getDefault()) }
	val fmtDia = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }

	// --- DIÁLOGO: ELIMINAR ---
	if (viewModel.showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { viewModel.onCloseDeleteDialog() },
			text = { Text("¿Seguro que quieres eliminar este pedido?") },
			confirmButton = {
				TextButton(
					onClick = {
						// Pasamos el objeto pedido completo al ViewModel
						viewModel.eliminarPedido(
							pedido = pedido,
							onBorradoExitoso = {
								onBorradoExitoso(pedido)
							},
							onError = {
								Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
							}
						)
					},
					enabled = !viewModel.isDeleting
				) {
					if (viewModel.isDeleting) {
						CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
					} else {
						Text("ELIMINAR", color = MaterialTheme.colorScheme.error)
					}
				}
			},
			dismissButton = {
				TextButton(onClick = { viewModel.onCloseDeleteDialog() }) { Text("CANCELAR") }
			}
		)
	}

	// --- DIÁLOGO: DESBLOQUEO (AZUL) ---
	if (showUnlockDialog) {
		AlertDialog(
			onDismissRequest = { },
			title = { Text("Pedido Documentado") },
			text = { Text("Este pedido ya está documentado (AZUL). ¿Quieres editarlo de todas formas?") },
			confirmButton = {
				TextButton(onClick = {
					onEditarClick(pedido)
				}) { Text("EDITAR") }
			},
			dismissButton = {
				TextButton(onClick = { }) { Text("CANCELAR") }
			}
		)
	}

	Scaffold { innerPadding ->
		BaseLayout(
			titulo = "Detalles Pedido",
			onBack = onVolver,
			modifier = Modifier.padding(innerPadding),
			actions = {
				IconButton(onClick = { if (esAzul) showUnlockDialog = true else onEditarClick(pedido) }) {
					Icon(
						Icons.Default.Edit,
						null,
						tint = if (esAzul) Color.Gray else MaterialTheme.colorScheme.primary
					)
				}
				IconButton(onClick = { viewModel.onOpenDeleteDialog() }) {
					Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
				}
			}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp)
			) {
				Text("ID: ${pedido.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
				Text(
					pedido.proveedor ?: "Sin proveedor",
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold
				)

				HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

				SeccionTitulo("Información de Control")
				DatoFila("Mes Correspondiente", fmtMes.format(pedido.meses))
				DatoFila("Cuenta Contable", pedido.cuenta)
				DetalleCajaTexto("Descripción", pedido.descripcion ?: "Sin descripción")

				Spacer(modifier = Modifier.height(16.dp))

				SeccionTitulo("Logística y Seguimiento")
				DatoFila("Nº Pedido", pedido.numPedido ?: "---")
				DatoFila("Nº Albarán", pedido.numAlbaran ?: "---")
				DatoFila("Referencia MRW", pedido.mrw ?: "---")
				DatoFila("Fecha Pedido", fmtDia.format(pedido.fechaPedido))
				DatoFila("Recepción", pedido.fechaRecibido?.let { fmtDia.format(it) } ?: "Pendiente")

				Spacer(modifier = Modifier.height(16.dp))

				SeccionTitulo("Economía")
				DatoFila("Cantidad", pedido.cantidad.toString())
				DatoFila("Precio Base", pedido.precio.toCurrency())
				DatoFila("Descuento", "${pedido.descuento} %")

				// Estos campos se han eliminado del desglose detallado
				// para evitar ruido visual, ya que el usuario busca el TOTAL.

				DatoFila("Nº Factura", pedido.numFactura ?: "No emitida")
				DatoFila("Vencimiento (VTO)", pedido.vto?.let { fmtDia.format(it) } ?: "---")

				HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

				DatoFila("Total Facturado", pedido.totalFra?.toCurrency() ?: "0.00 €")

				Surface(
					modifier = Modifier.padding(top = 20.dp, bottom = 60.dp),
					color = Color(0xFFFF5722).copy(alpha = 0.1f),
					shape = RoundedCornerShape(12.dp)
				) {
					Row(
						modifier = Modifier
							.padding(16.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text("PRECIO TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
						Text(
							pedido.precioTotal.toCurrency(),
							fontWeight = FontWeight.ExtraBold,
							fontSize = 18.sp,
							color = Color(0xFFFF5722)
						)
					}
				}
			}
		}
	}
}

/**
 * Componente interno para cajas de texto descriptivas (Descripción, MRW)
 */
@Composable
fun DetalleCajaTexto(label: String, contenido: String) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
				shape = RoundedCornerShape(8.dp)
			)
			.padding(12.dp)
	) {
		Text(
			text = label.uppercase(),
			style = MaterialTheme.typography.labelMedium,
			color = Color.Gray,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = contenido,
			style = MaterialTheme.typography.bodyLarge,
			lineHeight = 22.sp
		)
	}
}


/*
@Composable
fun DetailPedidoScreen(
	pedido: Pedido,
	viewModel: DetailPedidoViewModel? = if (LocalInspectionMode.current) null else viewModel(),
	onVolver: () -> Unit,
	onEditarClick: (Pedido) -> Unit,
	onBorradoExitoso: (Pedido) -> Unit
) {
	val context = LocalContext.current
	val esAzul = pedido.estado.equals("AZUL")
	// val esAzul = pedido.estado == "AZUL"
	var showUnlockDialog by remember { mutableStateOf(false) }

	val fmtMes = java.text.SimpleDateFormat("MMM yy", java.util.Locale.getDefault())
	val fmtDia = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

	val showDeleteDialog = viewModel?.showDeleteDialog ?: false
	if (showUnlockDialog) {
		AlertDialog(
			onDismissRequest = { showUnlockDialog = false },
			title = { Text("Pedido Documentado") },
			text = { Text("Este pedido YA está documentado (AZUL). ¿Estás seguro de que quieres editarlo?") },
			confirmButton = {
				TextButton(
					onClick = {
						showUnlockDialog = false
						onEditarClick(pedido)
					}) { Text("EDITAR", color = MaterialTheme.colorScheme.primary) }
			},
			dismissButton = {
				TextButton(onClick = { showUnlockDialog = false }) { Text("CANCELAR") }
			}
		)
	}

	if (showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { viewModel.onCloseDeleteDialog() },
			text = { Text("¿Seguro que quieres eliminar este pedido?") },
			confirmButton = {
				TextButton(
					onClick = {
						Log.d("NAVEGACION", "Llamando a onBorradoExitoso")
						viewModel.eliminarPedido(
							pedido = pedido,
							onBorradoExitoso = {
								Toast.makeText(context, "Pedido eliminado", Toast.LENGTH_SHORT).show()
								onBorradoExitoso(pedido)
							},
							onError = {
								Toast.makeText(context, "Error al eliminar pedido", Toast.LENGTH_SHORT).show()
							}
						)
					},
					enabled = !viewModel.isDeleting
				) {
					if (viewModel.isDeleting) {
						CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
					} else {
						Text("ELIMINAR", color = MaterialTheme.colorScheme.error)
					}
				}
			},
			dismissButton = {
				TextButton(onClick = { viewModel.onCloseDeleteDialog() }) { Text("CANCELAR") }
			}
		)
	}
	Scaffold { innerPadding ->
		if (pedido.id.isEmpty()) return@Scaffold
		BaseLayout(
			titulo = "Detalles Pedido",
			onBack = onVolver,
			modifier = Modifier.padding(innerPadding),
			actions = {
				IconButton(onClick = {
					if (esAzul) showUnlockDialog = true else onEditarClick(pedido)
				}) {
					Icon(
						Icons.Default.Edit,
						contentDescription = "Editar",
						tint = if (esAzul) Color.Gray else MaterialTheme.colorScheme.primary
					)
				}
				IconButton(onClick = { viewModel?.onOpenDeleteDialog() }) {
					Icon(
						Icons.Default.Delete,
						contentDescription = "Eliminar",
						tint = MaterialTheme.colorScheme.error
					)
				}
			}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				Text(
					text = "ID: ${pedido.id}",
					style = MaterialTheme.typography.bodySmall,
					color = Color.Gray
				)
				Text(
					pedido.proveedor ?: "Sin proveedor",
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold
				)

				HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

				// 1. INFORMACIÓN DE CONTROL (3 campos)
				SeccionTitulo("Información de Control")
				DatoFila("Mes Correspondiente", pedido.meses.let { fmtMes.format(it) } ?: "---")
				DatoFila("Cuenta Contable", pedido.cuenta)
				Spacer(modifier = Modifier.height(8.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
							shape = RoundedCornerShape(8.dp)
						)
						.padding(12.dp)
				) {
					Text(
						text = "DESCRIPCIÓN",
						style = MaterialTheme.typography.labelMedium,
						color = Color.Gray,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = pedido.descripcion ?: "Sin descripción",
						style = MaterialTheme.typography.bodyLarge,
						lineHeight = 22.sp // Mejora la lectura de textos largos
					)
				}
				Spacer(modifier = Modifier.height(16.dp))

				// 2. LOGÍSTICA (6 campos)
				SeccionTitulo("Logística y Seguimiento")
				DatoFila("Nº Pedido", pedido.numPedido ?: "Sin número")
				DatoFila("Nº Albarán", pedido.numAlbaran ?: "No disponible")
				Spacer(modifier = Modifier.height(8.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
							shape = RoundedCornerShape(8.dp)
						)
						.padding(12.dp)
				) {
					Text(
						text = "Referencia MRW",
						style = MaterialTheme.typography.labelMedium,
						color = Color.Gray,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = pedido.mrw ?: "---",
						style = MaterialTheme.typography.bodyLarge,
						lineHeight = 22.sp
					)
				}
				Spacer(modifier = Modifier.height(16.dp))
				DatoFila("Fecha de Pedido", pedido.fechaPedido.let { fmtDia.format(it) } ?: "---")
				DatoFila(
					"Fecha de Recepción",
					pedido.fechaRecibido?.let { fmtDia.format(it) } ?: "No recibido")
				DatoFila("Fecha Factura", pedido.fechaFra?.let { fmtDia.format(it) } ?: "Pendiente")

				// 3. ECONOMÍA DETALLADA (7 campos)
				SeccionTitulo("Desglose Económico")
				DatoFila("Cantidad", pedido.cantidad.toString())
				DatoFila("Precio Base Total", pedido.precio.toCurrency())
				DatoFila("Descuento Aplicado", "${pedido.descuento} %")
				DatoFila("Precio Unitario (Base)", pedido.precioUnidad.toCurrency())
				DatoFila("Precio Unitario (Con Desc.)", pedido.precioUnidadDescuento.toCurrency())
				DatoFila("Nº Factura", pedido.numFactura ?: "No emitida")
				DatoFila("Vencimiento (VTO)", pedido.vto?.let { fmtDia.format(it) } ?: "---")

				HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

				// 4. TOTALES (3 campos importantes)
				DatoFila("Total Facturado", pedido.totalFra?.toCurrency() ?: "0.00 €")

				Surface(
					modifier = Modifier.padding(top = 20.dp, bottom = 60.dp),
					color = Color(0xFFFF5722).copy(alpha = 0.1f),
					shape = MaterialTheme.shapes.medium
				) {
					Row(
						modifier = Modifier
							.padding(16.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text("PRECIO TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
						Text(
							pedido.precioTotal.toCurrency(),
							fontWeight = FontWeight.ExtraBold,
							fontSize = 18.sp,
							color = Color(0xFFFF5722)
						)
					}
				}
			}
		}
	}
}
*/
@Composable
fun SeccionTitulo(titulo: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			text = titulo.uppercase(),
			style = MaterialTheme.typography.labelLarge,
			color = Color(0xFFFF5722),
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 8.dp)
		)
	}
}

@Composable
fun DatoFila(label: String, valor: String?) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(label, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
		Text(valor ?: "---")
	}
}