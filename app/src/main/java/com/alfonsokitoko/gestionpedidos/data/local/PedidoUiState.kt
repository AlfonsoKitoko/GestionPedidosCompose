package com.alfonsokitoko.gestionpedidos.data.local

data class PedidoUiState(
	val meses: Long? = System.currentTimeMillis(),
	val proveedor: String = "",
	val descripcion: String = "",
	val cantidad: String = "",
	val precio: String = "",
	val descuento: String = "",
	val fechaPedido: Long? = System.currentTimeMillis(),
	val numPedido: String = "",
	val fechaRecibido: Long? = null,
	val numAlbaran: String = "",
	val fechaFra: Long? = null,
	val vto: Long? = null,
	val numFactura: String = "",
	val totalFra: String = "",
	val cuenta: String = "",
	val mrw: String = "",
	// Campos calculados para mostrar en UI
	val precioUnidad: String = "0,00 €",
	val precioUnidadDescuento: String = "0,00 €",
	val precioTotal: String = "0,00 €"
)
