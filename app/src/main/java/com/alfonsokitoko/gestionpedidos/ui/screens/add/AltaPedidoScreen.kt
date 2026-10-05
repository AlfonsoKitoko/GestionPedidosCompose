package com.alfonsokitoko.gestionpedidos.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alfonsokitoko.gestionpedidos.ui.components.BaseLayout
import com.alfonsokitoko.gestionpedidos.ui.components.FieldFecha
import com.alfonsokitoko.gestionpedidos.ui.components.MonthYearPickerDialog
import com.alfonsokitoko.gestionpedidos.ui.components.SelectProveedor
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaPedidoScreen(
	viewModel: AltaPedidoViewModel = viewModel(),
	alFinalizar: () -> Unit
) {
	val context = LocalContext.current
	val formatMonthYear = remember { SimpleDateFormat("MMM/yy", Locale.getDefault()) }
	val stateUi = viewModel.state
	var guardando by remember { mutableStateOf(false) } // Para evitar doble clic

	Scaffold { innerPadding ->
		BaseLayout(
			titulo = "Nuevo pedido",
			modifier = Modifier.padding(innerPadding),
			onBack = { alFinalizar() }
		) {
			Column(
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(12.dp) // Espaciado uniforme
			) {
				Spacer(modifier = Modifier.height(8.dp))

				// --- SECCIÓN: GENERAL ---
				SeccionTitulo("Información General")

				// Selector Mes/Año (Tu lógica de Box invisible es perfecta)
				var showMonthPicker by remember { mutableStateOf(false) }
				Box(modifier = Modifier.fillMaxWidth()) {
					OutlinedTextField(
						value = stateUi.meses?.let { formatMonthYear.format(it) } ?: "",
						onValueChange = { },
						label = { Text("Mes (mm/yy)") },
						readOnly = true,
						modifier = Modifier.fillMaxWidth(),
						trailingIcon = { Icon(Icons.Default.DateRange, null) }
					)
					Box(
						Modifier
							.matchParentSize()
							.clickable { showMonthPicker = true })
				}

				if (showMonthPicker) {
					MonthYearPickerDialog(
						fechaSeleccionada = stateUi.meses,
						onFechaSelected = { viewModel.onFieldChanged(meses = it); showMonthPicker = false },
						onDismiss = { showMonthPicker = false })
				}

				SelectProveedor(
					selectedProveedor = stateUi.proveedor,
					onProveedorSelected = { viewModel.onFieldChanged(proveedor = it) }
				)

				OutlinedTextField(
					value = stateUi.descripcion,
					onValueChange = { viewModel.onFieldChanged(descripcion = it) },
					label = { Text("Descripción") },
					modifier = Modifier.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
				)

				// --- SECCIÓN: CÁLCULOS ---
				SeccionTitulo("Cantidades y Precios")

				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					OutlinedTextField(
						value = stateUi.cantidad,
						onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onFieldChanged(cantidad = it) },
						label = { Text("Cant.") },
						modifier = Modifier.weight(1f),
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Number,
							imeAction = ImeAction.Next
						)
					)
					OutlinedTextField(
						value = stateUi.precio,
						onValueChange = { viewModel.onFieldChanged(precio = it) },
						label = { Text("Precio Unit.") },
						modifier = Modifier.weight(2f),
						suffix = { Text("€") },
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Decimal,
							imeAction = ImeAction.Next
						)
					)
				}

				OutlinedTextField(
					value = stateUi.descuento,
					onValueChange = { viewModel.onFieldChanged(descuento = it) },
					label = { Text("Descuento (%)") },
					modifier = Modifier.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Decimal,
						imeAction = ImeAction.Next
					)
				)

				// Campos de solo lectura para información del usuario
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					OutlinedTextField(
						value = stateUi.precioUnidadDescuento,
						onValueChange = {},
						label = { Text("Unit. con Desc.") },
						modifier = Modifier.weight(1f),
						readOnly = true,
						colors = TextFieldDefaults.colors(focusedContainerColor = Color.LightGray.copy(0.1f))
					)
					OutlinedTextField(
						value = stateUi.precioTotal,
						onValueChange = {},
						label = { Text("Total") },
						modifier = Modifier.weight(1f),
						readOnly = true,
						colors = TextFieldDefaults.colors(focusedContainerColor = Color.LightGray.copy(0.1f))
					)
				}

				// --- SECCIÓN: SEGUIMIENTO ---
				SeccionTitulo("Seguimiento y Logística")

				FieldFecha(
					"Fecha Pedido",
					stateUi.fechaPedido
				) { viewModel.onFieldChanged(fechaPedido = it) }
				OutlinedTextField(
					value = stateUi.numPedido,
					onValueChange = { viewModel.onFieldChanged(numPedido = it) },
					label = { Text("Nº Pedido") },
					modifier = Modifier.fillMaxWidth()
				)

				FieldFecha(
					"Fecha Recibido",
					stateUi.fechaRecibido
				) { viewModel.onFieldChanged(fechaRecibido = it) }
				OutlinedTextField(
					value = stateUi.numAlbaran,
					onValueChange = { viewModel.onFieldChanged(numAlbaran = it) },
					label = { Text("Nº Albarán") },
					modifier = Modifier.fillMaxWidth()
				)

				// --- SECCIÓN: FACTURACIÓN ---
				SeccionTitulo("Datos de Factura")

				OutlinedTextField(
					value = stateUi.numFactura,
					onValueChange = { viewModel.onFieldChanged(numFactura = it) },
					label = { Text("Nº Factura") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = stateUi.totalFra,
					onValueChange = { viewModel.onFieldChanged(totalFra = it, isManualTotalFra = true) },
					label = { Text("Total Factura") },
					modifier = Modifier.fillMaxWidth(),
					suffix = { Text("€") }
				)

				OutlinedTextField(
					value = stateUi.mrw,
					onValueChange = { viewModel.onFieldChanged(mrw = it) },
					label = { Text("MRW / Tracking") },
					modifier = Modifier.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
				)

				Spacer(modifier = Modifier.height(24.dp))

				Button(
					onClick = {
						guardando = true
						viewModel.guardarPedido(
							onSuccess = {
								guardando = false
								Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
								alFinalizar()
							},
							onError = {
								guardando = false
								Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
							}
						)
					},
					enabled = viewModel.formValido && !guardando,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					shape = RoundedCornerShape(12.dp)
				) {
					if (guardando) CircularProgressIndicator(
						color = Color.White,
						modifier = Modifier.size(24.dp)
					)
					else Text(if (viewModel.formValido) "GUARDAR PEDIDO" else "COMPLETAR DATOS")
				}

				Spacer(modifier = Modifier.height(32.dp))
			}
		}
	}
}

@Composable
fun SeccionTitulo(texto: String) {
	Text(
		text = texto.uppercase(),
		style = MaterialTheme.typography.labelLarge,
		color = MaterialTheme.colorScheme.primary,
		fontWeight = FontWeight.Bold,
		modifier = Modifier.padding(top = 8.dp)
	)
}