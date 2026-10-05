package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.alfonsokitoko.gestionpedidos.data.model.Constants.PROVEEDORES

// Selector + Buscador de proveedor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProveedor(
	selectedProveedor: String,
	onProveedorSelected: (String) -> Unit
) {
	var expanded by remember { mutableStateOf(false) }
	var searchQuery by remember { mutableStateOf(selectedProveedor) }

	LaunchedEffect(selectedProveedor) { searchQuery = selectedProveedor }

	val filtrados = remember(searchQuery) {
		if (searchQuery.isEmpty()) PROVEEDORES
		else PROVEEDORES.filter { it.contains(searchQuery, ignoreCase = true) }
	}
	ExposedDropdownMenuBox(
		expanded = expanded,
		onExpandedChange = { expanded = it },
		modifier = Modifier.fillMaxWidth()
	) {
		OutlinedTextField(
			modifier = Modifier
				.fillMaxWidth()
				.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable),
			value = searchQuery,
			onValueChange = {
				searchQuery = it
				expanded = true
			},
			label = { Text("Proveedor") },
			placeholder = { Text("--- Seleccionar Proveedor ---", color = Color.Gray) },
			trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
			colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
			singleLine = true
		)

		if (expanded) {
			ExposedDropdownMenu(
				expanded = expanded,
				onDismissRequest = {
					expanded = false
					if (searchQuery != selectedProveedor) searchQuery = selectedProveedor
				},
				modifier = Modifier.exposedDropdownSize()
			) {
				if (filtrados.isEmpty())
					DropdownMenuItem(
						text = { Text("Sin resultados", color = Color.Gray) },
						onClick = {},
						enabled = false
					)
				else filtrados.forEach { opt ->
					DropdownMenuItem(
						text = {
							Text(
								text = opt,
								fontWeight = if (opt == selectedProveedor) FontWeight.Bold else FontWeight.Normal
							)
						},
						onClick = {
							searchQuery = opt
							onProveedorSelected(opt)
							expanded = false
						},
						contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
					)
				}
			}
		}
	}
}