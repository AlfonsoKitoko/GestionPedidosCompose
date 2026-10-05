package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.alfonsokitoko.gestionpedidos.data.model.FiltroEstado

@Composable
fun SeccionFiltros(
	filtrosSeleccionados: Set<FiltroEstado>,
	onFiltroPulsado: (FiltroEstado) -> Unit
) {
	// Filtramos para no mostrar el botón de "TODOS"
	val filtros = FiltroEstado.entries

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		// Aquí SÍ tenemos control total del espacio
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		filtros.forEach { filtro ->
			val activo = filtrosSeleccionados.contains(filtro)
			val colorBase = filtro.getColor()

			// Usamos Surface para crear un botón personalizado y limpio
			Surface(
				selected = activo,
				onClick = { onFiltroPulsado(filtro) },
				shape = RoundedCornerShape(12.dp),
				// Color de fondo dinámico
				color = if (activo) colorBase.copy(alpha = 0.15f)
				else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
				// Forzamos que NO tenga borde
				border = null
			) {
				Row(
					modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					// CÍRCULO PERFECTO
					Box(
						modifier = Modifier
							.size(10.dp)
							.aspectRatio(1f) // Esto garantiza que sea un círculo
							.clip(CircleShape)
							.background(if (activo) colorBase else colorBase.copy(alpha = 0.4f))
					)

					Text(
						text = filtro.displayName,
						style = MaterialTheme.typography.labelLarge,
						color = if (activo) colorBase else MaterialTheme.colorScheme.onSurface,
						maxLines = 1
					)
				}
			}
		}
	}
}

/*
fun SeccionFiltros(
	filtroSeleccionado: FiltroEstado,
	onFiltroCambiado: (FiltroEstado) -> Unit
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth(),
		contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// Recorremos todos los valores de tu Enum FiltroEstado
		items(FiltroEstado.entries) { filtro ->
			val activo = filtroSeleccionado == filtro
			val colorBase = filtro.getColor()

			FilterChip(
				selected = filtroSeleccionado == filtro,
				onClick = { onFiltroCambiado(filtro) },
				label = {
					Text(
						text = filtro.displayName,
						fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
					)
				},
				leadingIcon = {
					Box(
						modifier = Modifier
							.size(12.dp)
							.clip(CircleShape)
							.background(if (activo) colorBase else colorBase.copy(alpha = 0.4f))
					)
				},
				border = FilterChipDefaults.filterChipBorder(
					enabled = true,
					selected = activo,
					borderColor = Color.Transparent,
					selectedBorderColor = Color.Transparent,
					borderWidth = 0.dp
				),
				colors = FilterChipDefaults.filterChipColors(
					selectedContainerColor = colorBase.copy(alpha = 0.25f),
					selectedLeadingIconColor = colorBase,
					selectedLabelColor = colorBase,

					containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
					labelColor = MaterialTheme.colorScheme.onSurfaceVariant
				),
				elevation = FilterChipDefaults
					.filterChipElevation(
						elevation = 0.dp,
						pressedElevation = 0.dp,
						focusedElevation = 0.dp,
						hoveredElevation = 0.dp,
						draggedElevation = 0.dp,
						disabledElevation = 0.dp
					)
			)
		}
	}
}*/
