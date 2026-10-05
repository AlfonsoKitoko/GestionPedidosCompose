package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils
import com.alfonsokitoko.gestionpedidos.utils.toCurrency
import java.text.SimpleDateFormat
import java.util.Locale

// Diseño de cada item de la lista de pedidos, vista muy simplificada con datos clave
@Composable
fun PedidoCard(
	pedido: Pedido,
	onClick: () -> Unit
) {
	// 1. Lógica de colores y estados
	val colorEstado = pedido.estadoCalculado
	val textoEstado = PedidoUtils.obtenerTextoEstado(pedido)
	val backgroundColorEstado = colorEstado.copy(alpha = 0.08f)
	val fmtMes = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

	// 2. Animación de "Latido" para el icono de sincronización pendiente
	val infiniteTransition = rememberInfiniteTransition(label = "sync_loop")
	val alphaAnim by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 0.4f,
		animationSpec = infiniteRepeatable(
			animation = tween(3000, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "alpha"
	)

	Card(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 6.dp),
		colors = CardDefaults.cardColors(containerColor = backgroundColorEstado),
		shape = MaterialTheme.shapes.medium,
		elevation = CardDefaults.cardElevation(
			defaultElevation = 0.dp,
			pressedElevation = 0.dp,
			focusedElevation = 0.dp,
			hoveredElevation = 0.dp,
			draggedElevation = 0.dp,
			disabledElevation = 0.dp
		)
	) {
		Box(modifier = Modifier.fillMaxWidth()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(IntrinsicSize.Min)
			) {
				// Barra lateral de color de estado
				Box(
					modifier = Modifier
						.fillMaxHeight()
						.width(6.dp)
						.background(colorEstado)
				)

				Column(
					modifier = Modifier
						.padding(16.dp)
						.fillMaxWidth()
				) {
					// --- CABECERA: Proveedor + Iconos de Estado ---
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = pedido.proveedor.uppercase(),
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Black,
							color = Color(0xFF1A1A1A),
							modifier = Modifier.weight(1f),
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)

						// ICONO DE SINCRONIZACIÓN (Más grande y animado)
						if (!pedido.synced) {
							Icon(
								imageVector = Icons.Default.CloudUpload,
								contentDescription = "Pendiente de subir",
								modifier = Modifier.size(22.dp), // Tamaño muy visible
								tint = Color(0xFFF44336).copy(alpha = alphaAnim) // Rojo parpadeante
							)
							Spacer(modifier = Modifier.width(12.dp))
						}

						// Badge de Texto de Estado
						Text(
							text = textoEstado,
							style = MaterialTheme.typography.labelSmall,
							fontWeight = FontWeight.Bold,
							color = colorEstado
						)
					}

					Spacer(modifier = Modifier.height(6.dp))

					// --- CUERPO: Descripción ---
					Text(
						text = pedido.descripcion.ifBlank { "Sin descripción" },
						style = MaterialTheme.typography.bodyMedium,
						color = Color.DarkGray,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)

					Spacer(modifier = Modifier.height(16.dp))

					// --- PIE: Info técnica y Precio ---
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.Bottom
					) {
						Column(modifier = Modifier.weight(1f)) {
							// Número de pedido
							val numPed = if (!pedido.numPedido.isNullOrBlank())
								PedidoUtils.obtenerNumeroPedidoLimpio(pedido) else "---"

							Text(
								text = "Nº PEDIDO: $numPed",
								style = MaterialTheme.typography.labelSmall,
								color = Color.Gray,
								fontWeight = FontWeight.Bold
							)
							Text(
								text = "CANTIDAD: ${pedido.cantidad}",
								style = MaterialTheme.typography.labelSmall,
								color = Color.Gray
							)
						}

						// Bloque de Precio y Fecha
						Column(horizontalAlignment = Alignment.End) {
							Text(
								text = fmtMes.format(pedido.meses).uppercase(),
								style = MaterialTheme.typography.labelSmall,
								color = Color.Gray,
								modifier = Modifier.padding(bottom = 2.dp)
							)
							Text(
								text = pedido.precioTotal.toCurrency(),
								style = MaterialTheme.typography.titleLarge,
								fontWeight = FontWeight.ExtraBold,
								color = Color(0xFF1A1A1A)
							)
						}
					}
				}
			}

			// Icono de candado para pedidos bloqueados (AZUL)
			if (pedido.estado == "AZUL") {
				Icon(
					imageVector = Icons.Default.Lock,
					contentDescription = null,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(8.dp)
						.size(12.dp),
					tint = colorEstado.copy(alpha = 0.4f)
				)
			}
		}
	}
}