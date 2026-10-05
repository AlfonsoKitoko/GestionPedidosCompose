package com.alfonsokitoko.gestionpedidos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class PedidoDaoTest {

	private lateinit var database: AppDatabase
	private lateinit var dao: PedidoDao

	@Before
	fun setup() {
		database = Room.inMemoryDatabaseBuilder(
			ApplicationProvider.getApplicationContext(),
			AppDatabase::class.java
		).allowMainThreadQueries().build()
		dao = database.pedidoDao()
	}

	@After
	fun teardown() {
		database.close()
	}

	private fun createFullPedido(id: String, synced: Boolean = true, isDeleted: Boolean = false) =
		Pedido(
			id = id,
			meses = Date(),
			proveedor = "Proveedor $id",
			descripcion = "Desc $id",
			cantidad = 5,
			precio = 5000L,
			fechaPedido = Date(),
			numPedido = "NP-$id",
			fechaRecibido = null,
			numAlbaran = null,
			descuento = 10.0,
			precioUnidad = 1000L,
			precioUnidadDescuento = 900L,
			precioTotal = 4500L,
			fechaFra = null,
			vto = null,
			numFactura = null,
			totalFra = 4500L,
			cuenta = "700",
			mrw = null,
			synced = synced,         // Campo nuevo
			isDeleted = isDeleted,   // Campo nuevo
			createdAt = Date(),      // Campo nuevo
			updatedAt = Date()       // Campo nuevo
		)

	@Test
	fun insertAndRetrieveUnsyncedPedidos() = runBlocking {
		val p1 = createFullPedido("1", synced = true)
		val p2 = createFullPedido("2", synced = false) // Este debería ser detectado por el Worker

		dao.insertAllPedidos(listOf(p1, p2))

		val unsynced = dao.getPedidosUnsynced()

		assertEquals(1, unsynced.size)
		assertEquals("2", unsynced[0].id)
		assertFalse(unsynced[0].synced)
	}

	@Test
	fun markAsDeletedLogicalTest() = runBlocking {
		val pedido = createFullPedido("10")
		dao.insertPedido(pedido)

		// Simula lo que hace el Repository antes de que el SyncWorker actúe
		dao.markAsDeleted("10")

		val result = dao.getByIdPedido("10")
		assertNotNull(result)
		assertTrue(result!!.isDeleted) // Verificamos que el borrado es lógico primero
	}

	@Test
	fun getPedidosPendingDeleteTest() = runBlocking {
		val p1 = createFullPedido("1", isDeleted = true)
		val p2 = createFullPedido("2", isDeleted = false)

		dao.insertAllPedidos(listOf(p1, p2))

		val pendingDelete = dao.getPedidosPendingDelete()

		assertEquals(1, pendingDelete.size)
		assertEquals("1", pendingDelete[0].id)
	}

	@Test
	fun deletePermanentTest() = runBlocking {
		val pedido = createFullPedido("99")
		dao.insertPedido(pedido)

		dao.deletePermanent("99") // El borrado físico que hace el Worker tras éxito en API

		val result = dao.getByIdPedido("99")
		assertNull(result)
	}
}