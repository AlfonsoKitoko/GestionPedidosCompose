package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfonsokitoko.gestionpedidos.data.model.FiltrosAvanzados

@Composable
fun FiltrosDrawerContent(
	filtros: FiltrosAvanzados,
	proveedoresDisponibles: List<String>,
	onFiltrosCambiados: (FiltrosAvanzados) -> Unit,
	onLimpiar: () -> Unit
) {
	ModalDrawerSheet {
		Text(
			"Filtros Avanzados",
			modifier = Modifier.padding(16.dp),
			style = MaterialTheme.typography.titleLarge
		)
		HorizontalDivider()

		// --- FILTRO POR PROVEEDOR ---
		Text(
			"Proveedor",
			modifier = Modifier.padding(16.dp),
			style = MaterialTheme.typography.labelLarge
		)
		LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
			items(proveedoresDisponibles) { prov ->
				NavigationDrawerItem(
					label = { Text(prov) },
					selected = filtros.proveedor == prov,
					onClick = { onFiltrosCambiados(filtros.copy(proveedor = if (filtros.proveedor == prov) null else prov)) },
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)
			}
		}

		Text("Mes", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelLarge)
		val meses = listOf(
			"Enero",
			"Febrero",
			"Marzo",
			"Abril",
			"Mayo",
			"Junio",
			"Julio",
			"Agosto",
			"Septiembre",
			"Octubre",
			"Noviembre",
			"Diciembre"
		)

		FlowRow(modifier = Modifier.padding(16.dp), maxItemsInEachRow = 3) {
			meses.forEachIndexed { index, mes ->
				FilterChip(
					selected = filtros.month == index,
					onClick = { onFiltrosCambiados(filtros.copy(month = if (filtros.month == index) null else index)) },
					label = { Text(mes, fontSize = 12.sp) },
					modifier = Modifier.padding(2.dp)
				)
			}
		}

		Spacer(Modifier.weight(1f))

		Button(
			onClick = onLimpiar,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxWidth()
				.padding(16.dp),
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
		) {
			Text("Limpiar Filtros")
		}
	}
}