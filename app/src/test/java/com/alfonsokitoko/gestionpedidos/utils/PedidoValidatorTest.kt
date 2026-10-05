package com.alfonsokitoko.gestionpedidos.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoValidatorTest {

	@Test
	fun `esEnteroValido returns true for positive integers`() {
		assertTrue(PedidoValidator.esEnteroValido("10"))
		assertTrue(PedidoValidator.esEnteroValido("0"))
	}

	@Test
	fun `esEnteroValido returns false for invalid input`() {
		assertFalse(PedidoValidator.esEnteroValido("-1"))
		assertFalse(PedidoValidator.esEnteroValido("abc"))
		assertFalse(PedidoValidator.esEnteroValido(""))
		assertFalse(PedidoValidator.esEnteroValido("  "))
	}

	@Test
	fun `esDecimalValido returns true for valid decimals`() {
		assertTrue(PedidoValidator.esDecimalValido("10.5"))
		assertTrue(PedidoValidator.esDecimalValido("10,5"))
		assertTrue(PedidoValidator.esDecimalValido("10"))
	}

	@Test
	fun `esDecimalValido returns false for invalid input`() {
		assertFalse(PedidoValidator.esDecimalValido("abc"))
		assertFalse(PedidoValidator.esDecimalValido(""))
	}

	@Test
	fun `esTextoValido returns true for length greater equal 3`() {
		assertTrue(PedidoValidator.esTextoValido("abc"))
		assertTrue(PedidoValidator.esTextoValido("long text"))
	}

	@Test
	fun `esTextoValido returns false for length lesser 3`() {
		assertFalse(PedidoValidator.esTextoValido("ab"))
		assertFalse(PedidoValidator.esTextoValido(""))
	}

	@Test
	fun `esProveedorValido returns false for default value`() {
		assertFalse(PedidoValidator.esProveedorValido("-- Proveedor --"))
		assertFalse(PedidoValidator.esProveedorValido(""))
	}

	@Test
	fun `esProveedorValido returns true for valid provider name`() {
		assertTrue(PedidoValidator.esProveedorValido("Generico"))
	}
}