package com.alfonsokitoko.gestionpedidos.utils

object PedidoValidator {
	fun esEnteroValido(valor: String): Boolean {
		return valor.isNotBlank() && valor.toIntOrNull() != null && valor.toInt() >= 0
	}

	fun esDecimalValido(valor: String): Boolean {
		return valor.isNotBlank() && valor.replace(",", ".").toDoubleOrNull() != null
	}

	fun esTextoValido(texto: String): Boolean {
		return (texto.trim().length) >= 3
	}

	fun esProveedorValido(valor: String): Boolean {
		return valor.isNotBlank() &&
				valor != "-- Proveedor --" &&
				valor != "Seleccionar Proveedor"
	}
}