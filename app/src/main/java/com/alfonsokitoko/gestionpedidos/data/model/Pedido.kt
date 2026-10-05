package com.alfonsokitoko.gestionpedidos.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.alfonsokitoko.gestionpedidos.data.local.SkipSerializing
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@Entity(tableName = "pedidos")
@JsonClass(generateAdapter = true)
data class Pedido(
	@PrimaryKey
	@get:SkipSerializing
	@param:Json(name = "_id")    // Mapeo del ID de MongoDB
	val id: String,  // Se genera un id temporal
	// Campos del pedido
	val meses: Date,                  // Mes del pedido (formato MMM/yy)
	val proveedor: String,
	val descripcion: String,
	val cantidad: Int,
	val precio: Long,                 // Almacenado en céntimos para evitar error Float
	val descuento: Double,
	val precioTotal: Long,

	//	Determinan el estado Recibido (VERDE)
	val fechaPedido: Date,            // Fecha de emisión del pedido (formato dd/MM/yyyy)
	val numPedido: String?,
	val fechaRecibido: Date?,  // Fecha de recepción del pedido (formato dd/MM/yyyy)
	val numAlbaran: String?,          // Número de Albarán

	//	Determinan el estado Documentado (AZUL)
	val fechaFra: Date?,       // Fecha Facturada (formato: dd/MM/yyyy)
	val vto: Date?,            // Fecha Vencimiento (formato: dd/MM/yyyy)
	val numFactura: String?,
	val totalFra: Long?,

	val cuenta: String,
	val mrw: String?,

	// +++ METADATA +++
	@param:Json(name = "createdAt") val createdAt: Date? = null,
	@param:Json(name = "updatedAt") val updatedAt: Date? = null,
	val synced: Boolean = false,
	val isDeleted: Boolean = false,

// +++ VIRTUALES: Solo lectura (GSON los llena, Room los ignora) +++	@Ignore
	@Ignore @param:Json(name = "estado")
	val estado: String? = null,       // ROJO / VERDE / AZUL
	@Ignore @param:Json(name = "precioEuros")
	val precioUnidadEuros: String? = null,
	@Ignore @param:Json(name = "precioUnidadDescuento")
	val precioUnidadDescuentoCalculado: String? = null,
	@Ignore @param:Json(name = "precioTotalEuros")
	val precioTotalEuros: String? = null
) {
	constructor(
		id: String,
		meses: Date,
		proveedor: String,
		descripcion: String,
		cantidad: Int,
		precio: Long,
		descuento: Double,
		precioTotal: Long,
		fechaPedido: Date,
		numPedido: String?,
		fechaRecibido: Date?,
		numAlbaran: String?,
		fechaFra: Date?,
		vto: Date?,
		numFactura: String?,
		totalFra: Long?,
		cuenta: String,
		mrw: String?,
		createdAt: Date?,
		updatedAt: Date?,
		synced: Boolean,
		isDeleted: Boolean
	) : this(
		id, meses, proveedor, descripcion, cantidad, precio, descuento, precioTotal,
		fechaPedido, numPedido, fechaRecibido, numAlbaran, fechaFra, vto, numFactura,
		totalFra, cuenta, mrw, createdAt, updatedAt, synced, isDeleted,
		null, null, null, null // Los campos @Ignore se pasan como null
	)

	val estadoCalculado: Color get() = PedidoUtils.obtenerColorEstado(this)
}