package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Componente para la entrada de fechas estándar (día/mes/año)
// Usa DatePicker de Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldFecha(
	label: String,
	selectedFecha: Long?,  // Timestamp en milisegundos
	onSelectedFecha: (Long) -> Unit
) {
	var showModal by remember { mutableStateOf(false) }
	// Estado del DatePicker de Material 3
	val datePickerState = rememberDatePickerState()

	// Formateador de fecha para que sea legible por el usuario
	val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
	val fechaTexto = selectedFecha?.let { formatter.format(Date(it)) } ?: ""

	// Contenedor del TextField
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { showModal = true }
	) {
		OutlinedTextField(
			value = fechaTexto,
			onValueChange = {},
			label = { Text(label) },
			readOnly = true,
			enabled = false,
			colors = OutlinedTextFieldDefaults.colors(
				focusedBorderColor = MaterialTheme.colorScheme.primary,
				unfocusedBorderColor = MaterialTheme.colorScheme.outline,
				disabledTextColor = MaterialTheme.colorScheme.onSurface,
				disabledBorderColor = MaterialTheme.colorScheme.outline,
				disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
			),
			trailingIcon = { Icon(Icons.Default.DateRange, null) }
		)
	}

	// Lógica del Dialog de Material 3
	if (showModal) {
		DatePickerDialog(
			onDismissRequest = { showModal = false },
			confirmButton = {
				TextButton(onClick = {
					// Si el usuario selecciona fecha, se devuelve
					datePickerState.selectedDateMillis?.let { onSelectedFecha(it) }
					showModal = false
				}) { Text("Aceptar") }
			},
			dismissButton = {
				TextButton(onClick = { showModal = false }) { Text("Cancelar") }
			}
		) {
			DatePicker(state = datePickerState)
		}
	}
}

// Diálogo personalizado para seleccionar mes y año (mm/YY)
@Composable
fun MonthYearPickerDialog(
	fechaSeleccionada: Long?,
	onFechaSelected: (Long) -> Unit,
	onDismiss: () -> Unit
) {
	// Inicializa calendario con la fecha actual o la seleccionada
	val calendario = remember(fechaSeleccionada) {
		Calendar.getInstance().apply {
			if (fechaSeleccionada != null) timeInMillis = fechaSeleccionada
		}
	}

	val meses =
		listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
	val yearActual = Calendar.getInstance().get(Calendar.YEAR)
	val years = (yearActual - 5..yearActual + 5).toList() // Rango de 10 años

	var mesSeleccionado by remember { mutableIntStateOf(calendario.get(Calendar.MONTH)) }
	var yearSeleccionado by remember { mutableIntStateOf(calendario.get(Calendar.YEAR)) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Seleccionar Mes y Año") },
		text = {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 16.dp),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Selector de Mes
				NumberPicker(
					value = mesSeleccionado,
					range = meses,
					onValueChange = { mesSeleccionado = it }
				)
				// Selector de Año
				NumberPicker(
					value = years.indexOf(yearSeleccionado),
					range = years.map { it.toString() },
					onValueChange = { yearSeleccionado = years[it] }
				)
			}
		},
		confirmButton = {
			TextButton(onClick = {
				// Constructor de fecha normalizada (Día 1 del mes a las 00:00:00) para el schema de mongoose
				val cal = Calendar.getInstance().apply {
					set(Calendar.YEAR, yearSeleccionado)
					set(Calendar.MONTH, mesSeleccionado)
					set(Calendar.DAY_OF_MONTH, 1) // Siempre el día 1
					// Exactamente al inicio del día
					set(Calendar.HOUR_OF_DAY, 0)
					set(Calendar.MINUTE, 0)
					set(Calendar.SECOND, 0)
					set(Calendar.MILLISECOND, 0)
				}
				onFechaSelected(cal.timeInMillis)
				onDismiss()
			}) { Text("Aceptar") }
		},
		dismissButton = {
			TextButton(onClick = onDismiss) { Text("Cancelar") }
		}
	)
}

// Un selector simple de tipo rueda
@Composable
fun NumberPicker(
	value: Int,
	range: List<String>,
	onValueChange: (Int) -> Unit,
) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		LazyColumn(
			modifier = Modifier
				.height(150.dp)
				.width(100.dp)
		) {
			itemsIndexed(range) { index, item ->
				Text(
					text = item,
					modifier = Modifier
						.fillMaxWidth()
						.clip(RoundedCornerShape(8.dp))
						.background(if (index == value) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
						.clickable { onValueChange(index) }
						.padding(12.dp),
					textAlign = TextAlign.Center,
					color = if (index == value) MaterialTheme.colorScheme.primary else Color.Unspecified
				)
			}
		}
	}
}