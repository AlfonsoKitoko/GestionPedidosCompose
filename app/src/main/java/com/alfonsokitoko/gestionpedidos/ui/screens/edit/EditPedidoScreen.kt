package com.alfonsokitoko.gestionpedidos.ui.screens.edit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.ui.components.BaseLayout
import com.alfonsokitoko.gestionpedidos.ui.components.FieldFecha
import com.alfonsokitoko.gestionpedidos.ui.components.MonthYearPickerDialog
import com.alfonsokitoko.gestionpedidos.ui.components.SelectProveedor
import com.alfonsokitoko.gestionpedidos.ui.screens.add.SeccionTitulo
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPedidoScreen(
	pedidoOld: Pedido,
	viewModel: EditPedidoViewModel = viewModel(),
	alFinalizar: (Pedido) -> Unit,
	onCancelar: () -> Unit
) {
	val context = LocalContext.current
	val formatMonthYear = remember { SimpleDateFormat("MM/yy", Locale.getDefault()) }
	val stateUi = viewModel.state

	// Estado para el diálogo de confirmación de borrado
	var mostrarDialogoBorrado by remember { mutableStateOf(false) }

	// Inicializamos el ViewModel con los datos del pedido a editar
	LaunchedEffect(pedidoOld.id) {
		viewModel.init(pedidoOld)
	}

	Scaffold(
		topBar = {
			// Podrías añadir un icono de papelera aquí si prefieres
		}
	) { innerPadding ->
		BaseLayout(
			titulo = "Editar Pedido",
			modifier = Modifier.padding(innerPadding),
			onBack = onCancelar,
			actions = {
				// Botón de borrar en la barra superior para mayor comodidad
				IconButton(onClick = { mostrarDialogoBorrado = true }) {
					Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
				}
			}
		) {
			Column(
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Spacer(modifier = Modifier.height(8.dp))

				// --- SELECTOR MES/AÑO ---
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

				// --- RESTO DE CAMPOS (REUTILIZADOS DE ALTA) ---
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

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Button(
						modifier = Modifier
							.fillMaxWidth()
							.height(56.dp),
						onClick = {
							viewModel.actualizarPedido(
								onSuccess = { pedidoUpdated ->
									alFinalizar(pedidoUpdated)
								},
								onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
							)
						},
						enabled = viewModel.formValido && !viewModel.isSaving
					) {
						if (viewModel.isSaving) {
							CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
						} else {
							Text("GUARDAR CAMBIOS")
						}
					}

					OutlinedButton(
						modifier = Modifier.fillMaxWidth(),
						onClick = onCancelar,
						border = BorderStroke(1.dp, Color.Gray)
					) {
						Text("CANCELAR", color = Color.Gray)
					}
				}

				Spacer(modifier = Modifier.height(40.dp))
			}
		}
	}

	// --- DIÁLOGO DE CONFIRMACIÓN DE BORRADO ---
	if (mostrarDialogoBorrado) {
		AlertDialog(
			onDismissRequest = { },
			title = { Text("¿Eliminar pedido?") },
			text = { Text("El pedido se marcará como eliminado y se sincronizará con el servidor.") },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.eliminarPedido(
							onSuccess = {
								onCancelar()
							},
							onError = { /* Error */ }
						)
					}
				) { Text("ELIMINAR", color = Color.Red) }
			},
			dismissButton = {
				TextButton(onClick = { }) { Text("CANCELAR") }
			}
		)
	}
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPedidoScreen(
	pedidoOld: Pedido,
	viewModel: EditPedidoViewModel = viewModel(),
	alFinalizar: (Pedido) -> Unit,
	onCancelar: () -> Unit
) {
	val context = LocalContext.current
	val formatMonthYear = SimpleDateFormat("MM/yy", Locale.getDefault())

	LaunchedEffect(pedidoOld) {
		viewModel.init(pedidoOld)
	}

	Scaffold { innerPadding ->
		Column(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.fillMaxSize()
		) {
			// Cabecera
			Image(
				painter = painterResource(id = R.drawable.logo_generico),
				contentDescription = "Logo Genérico",
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
					.height(60.dp),
				contentScale = ContentScale.Fit
			)
			Text(
				"Editar Pedido",
				fontSize = 20.sp,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
			)

			HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp)

			Column(
				modifier = Modifier
					.padding(16.dp)
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				// Selector Mes/Año
				var showMonthPicker by remember { mutableStateOf(false) }
				OutlinedTextField(
					value = formatMonthYear.format(Date(viewModel.meses)),
					onValueChange = { },
					label = { Text("Mes (mm/yy)") },
					readOnly = true,
					modifier = Modifier
						.fillMaxWidth()
						.clickable { showMonthPicker = true },
					colors = OutlinedTextFieldDefaults.colors(
						disabledTextColor = MaterialTheme.colorScheme.onSurface,
						disabledBorderColor = MaterialTheme.colorScheme.outline
					),
					trailingIcon = { Icon(Icons.Default.DateRange, null) }
				)

				if (showMonthPicker) {
					MonthYearPickerDialog(
						fechaSeleccionada = viewModel.meses,
						onFechaSelected = { viewModel.meses = it; showMonthPicker = false },
						onDismiss = { showMonthPicker = false }
					)
				}

				SelectProveedor(
					selectedProveedor = viewModel.proveedor,
					onProveedorSelected = {
						viewModel.proveedor = it
					}
				)

				OutlinedTextField(
					value = viewModel.descripcion,
					onValueChange = {
						viewModel.descripcion = it
					},
					label = { Text("Descripción") },
					modifier = Modifier.fillMaxWidth(),
					isError = viewModel.errorDescripcion,
					supportingText = { if (viewModel.errorDescripcion) Text("Descripción demasiado corta") },
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					)
				)

				OutlinedTextField(
					value = viewModel.cantidad,
					onValueChange = {
						if (it.all { c -> c.isDigit() }) {
							viewModel.cantidad = it
							viewModel.onInputsChanged()
						}
					},
					label = { Text("Cantidad") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Number,
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					isError = viewModel.errorCantidad,
					supportingText = { if (viewModel.errorCantidad) Text("Debe ser un número mayor que 0") }
				)

				OutlinedTextField(
					value = viewModel.precio,
					onValueChange = {
						viewModel.precio = it
						viewModel.onInputsChanged()
					},
					label = { Text("Precio") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Decimal,
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					suffix = { Text(" €") },
					isError = viewModel.errorPrecio,
					supportingText = { if (viewModel.errorPrecio) Text("Formato incorrecto") }
				)

				FieldFecha(
					"Fecha Pedido (dd/mm/yy)",
					viewModel.fechaPedido
				) {
					viewModel.fechaPedido = it
				}
				OutlinedTextField(
					value = viewModel.numPedido,
					onValueChange = {
						viewModel.numPedido = it
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					label = { Text("Nº Pedido") },
					modifier = Modifier.fillMaxWidth()
				)

				FieldFecha(
					"Fecha Recibido (dd/mm/yy)",
					viewModel.fechaRecibido
				) {
					viewModel.fechaRecibido = it
				}
				OutlinedTextField(
					value = viewModel.numAlbaran,
					onValueChange = {
						viewModel.numAlbaran = it
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					label = { Text("Nº Albarán") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = viewModel.descuento,
					onValueChange = {
						viewModel.descuento = it
						viewModel.onInputsChanged()
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Decimal,
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					label = { Text("Descuento (%)") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = viewModel.precioUnidadDescuento,
					onValueChange = { },
					label = { Text("Precio Unitario con Descuento") },
					modifier = Modifier.fillMaxWidth(),
					readOnly = true,
					suffix = { Text(" €") }
				)

				OutlinedTextField(
					value = viewModel.precioTotal,
					onValueChange = { },
					label = { Text("Precio Total") },
					modifier = Modifier.fillMaxWidth(),
					readOnly = true,
					suffix = { Text(" €") }
				)

				FieldFecha(
					"Fecha Factura",
					viewModel.fechaFra
				) {
					viewModel.fechaFra = it
				}
				FieldFecha("Vto", viewModel.vto) {
					viewModel.vto = it
				}

				OutlinedTextField(
					value = viewModel.numFactura,
					onValueChange = {
						viewModel.numFactura = it
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					label = { Text("Nº Factura") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = viewModel.totalFra,
					onValueChange = { viewModel.totalFra = it },
					label = { Text("Total Fra") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Decimal,
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					suffix = { Text(" €") }
				)

				OutlinedTextField(
					value = viewModel.cuenta,
					onValueChange = {
						viewModel.cuenta = it
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next // Esto pone una flecha de "Siguiente" en el teclado
					),
					label = { Text("Cuenta") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = viewModel.mrw,
					onValueChange = {
						viewModel.mrw = it
					},
					singleLine = true,
					maxLines = 1,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Done // Cierra el teclado al terminar
					),
					label = { Text("MRW") },
					modifier = Modifier.fillMaxWidth()
				)

				Spacer(modifier = Modifier.height(24.dp))

				Button(
					onClick = {
						viewModel.actualizarPedido(
							pedidoOld = pedidoOld,
							onSuccess = { pedidoUpdated -> alFinalizar(pedidoUpdated) },
							onError = {
								Toast.makeText(context, "Error al actualizar pedido", Toast.LENGTH_SHORT).show()
							}
						)
					},
					enabled = viewModel.formValido && !viewModel.isSaving
				) {
					if (viewModel.isSaving) {
						CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
					} else {
						Text("Guardar Cambios")
					}
				}

				Button(
					onClick = onCancelar,
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 8.dp),
					colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Gray)
				) {
					Text("Cancelar")
				}
				Spacer(modifier = Modifier.height(50.dp))
			}
		}
	}
}*/
