package com.alfonsokitoko.gestionpedidos.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {
	@TypeConverter  // de Timestamp a Date
	fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

	@TypeConverter  // de Date a Timestamp
	fun dateToTimestamp(date: Date?): Long? = date?.time
}