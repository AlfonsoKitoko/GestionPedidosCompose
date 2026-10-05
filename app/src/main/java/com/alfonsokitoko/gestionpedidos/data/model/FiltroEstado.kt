package com.alfonsokitoko.gestionpedidos.data.model

import androidx.compose.ui.graphics.Color

// Estado de los pedidos
enum class FiltroEstado(val displayName: String) {
	ROJO("Pto. Venir"),
	VERDE("Recibido"),
	AZUL("Documentado");

	// Color para cada estado
	fun getColor(): Color = when (this) {
		ROJO -> Color(0xFFFF5252)
		VERDE -> Color(0xFF4CAF50)
		AZUL -> Color(0xFF2196F3)
	}
}