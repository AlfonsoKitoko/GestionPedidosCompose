package com.alfonsokitoko.gestionpedidos.utils

import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class PedidoStatusUtilsTest {

	private fun createBasePedido() = Pedido(
		id = "1",
		meses = Date(),
		proveedor = "Test",
		descripcion = "Test",
		cantidad = 1,
		precio = 100L,
		fechaPedido = Date(),
		numPedido = "P1",
		fechaRecibido = null,
		numAlbaran = null,
		descuento = 0.0,
		precioUnidad = 100L,
		precioUnidadDescuento = 100L,
		precioTotal = 100L,
		fechaFra = null,
		vto = null,
		numFactura = null,
		totalFra = 100L,
		cuenta = "700",
		mrw = null,
		estado = null,      // Campo importante para la lógica
		synced = true,
		isDeleted = false,
		createdAt = Date(),
		updatedAt = Date()
	)

	@Test
	fun `obtenerColorEstado returns azulDocumentado when numFactura is present`() {
		val pedido = createBasePedido().copy(numFactura = "F-123")
		val color = PedidoUtils.obtenerColorEstado(pedido)
		assertEquals(PedidoUtils.azulDocumentado, color)
	}

	@Test
	fun `obtenerColorEstado returns verdeRecibido when numAlbaran is present and no factura`() {
		val pedido = createBasePedido().copy(numAlbaran = "A-123", numFactura = null)
		val color = PedidoUtils.obtenerColorEstado(pedido)
		assertEquals(PedidoUtils.verdeRecibido, color)
	}

	@Test
	fun `obtenerColorEstado returns rojoPtoVenir when no albaran and no factura`() {
		val pedido = createBasePedido().copy(numAlbaran = null, numFactura = null)
		val color = PedidoUtils.obtenerColorEstado(pedido)
		assertEquals(PedidoUtils.rojoPtoVenir, color)
	}

	@Test
	fun `obtenerTextoEstado returns correct text based on fields`() {
		val p1 = createBasePedido().copy(numFactura = "F1")
		assertEquals("DOCUMENTADO (FRA)", PedidoUtils.obtenerTextoEstado(p1))

		val p2 = createBasePedido().copy(numAlbaran = "A1", numFactura = null)
		assertEquals("RECIBIDO (ALB)", PedidoUtils.obtenerTextoEstado(p2))

		val p3 = createBasePedido().copy(numAlbaran = null, numFactura = null)
		assertEquals("PTO. VENIR MATERIAL", PedidoUtils.obtenerTextoEstado(p3))
	}
}