package com.alfonsokitoko.gestionpedidos.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PedidoUtilsTest {

	@Test
	fun `toCurrency converts cents to formatted string`() {
		val cents = 12345L
		// En un entorno con Locale es_ES, debería ser "123,45 €" o similar.
		// Como toCurrency usa NumberFormat.getCurrencyInstance(localeSpain),
		// forzamos la comparación con el formato esperado en España.
		val result = cents.toCurrency()
		// Nota: El espacio entre el número y el símbolo € puede ser un espacio no rompible (\u00A0)
		assert(result.contains("123,45"))
		assert(result.contains("€"))
	}

	@Test
	fun `currencyToCentimos converts formatted string to cents`() {
		val currencyString = "123,45 €"
		val result = currencyString.currencyToCentimos()
		assertEquals(12345L, result)
	}

	@Test
	fun `currencyToCentimos handles invalid input`() {
		val invalid = "abc"
		assertEquals(0L, invalid.currencyToCentimos())
	}

	@Test
	fun `calcTotales calculates unit and total prices correctly`() {
		val precio = "100"
		val cantidad = "2"
		val descuento = "10"

		val (u, ud, t) = PedidoUtils.calcTotales(precio, cantidad, descuento)

		// Si el precio es 100 y la cantidad 2:
		// u (unidad) -> "100,00"
		// ud (unidad con descuento 10%) -> "90,00"
		// t (total: 90 * 2) -> "180,00"

		assert(u.contains("100"))
		assert(ud.contains("90"))
		assert(t.contains("180"))
	}

	@Test
	fun `currencyToCentimos maneja formatos mixtos y espacios extraños`() {
		// Caso: Espacios de no rotura, puntos y comas mezclados
		val sucio = " 1.250,50 € "
		assertEquals(125050L, sucio.currencyToCentimos())

		// Caso: Solo el símbolo
		val soloSimbolo = "€"
		assertEquals(0L, soloSimbolo.currencyToCentimos())

		// Caso: Nulo o vacío
		val vacio = ""
		assertEquals(0L, vacio.currencyToCentimos())
	}
}