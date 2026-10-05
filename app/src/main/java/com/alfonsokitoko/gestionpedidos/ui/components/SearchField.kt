package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// TODO:
//  - agregar ordenaciones para la lista (filtros por fecha, mayor a menor y al revés filtros por proveedor, etc)
@Composable
fun SearchField(
	searchQuery: String,
	onSearchQueryChanged: (String) -> Unit,
) {
	TextField(
		value = searchQuery,
		onValueChange = onSearchQueryChanged,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		placeholder = { Text("Buscar...") },
		leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
		trailingIcon = {
			if (searchQuery.isNotEmpty()) {
				IconButton(
					onClick = {
						onSearchQueryChanged("")
					}) {
					Icon(
						(Icons.Default.Close),
						contentDescription = "Borrar"
					)
				}
			}
		},
		shape = RoundedCornerShape(12.dp),
		singleLine = true,
		colors = TextFieldDefaults.colors(
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
			disabledIndicatorColor = Color.Transparent,
			focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f),
			unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
		)
	)
}
