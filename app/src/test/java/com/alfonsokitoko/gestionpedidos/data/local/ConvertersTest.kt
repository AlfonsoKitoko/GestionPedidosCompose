package com.alfonsokitoko.gestionpedidos.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `fromTimestamp converts Long to Date`() {
        val timestamp = 1672531200000L // 2023-01-01
        val date = converters.fromTimestamp(timestamp)
        assertEquals(timestamp, date?.time)
    }

    @Test
    fun `fromTimestamp returns null for null input`() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `dateToTimestamp converts Date to Long`() {
        val timestamp = 1672531200000L
        val date = Date(timestamp)
        val result = converters.dateToTimestamp(date)
        assertEquals(timestamp, result)
    }

    @Test
    fun `dateToTimestamp returns null for null input`() {
        assertNull(converters.dateToTimestamp(null))
    }
}