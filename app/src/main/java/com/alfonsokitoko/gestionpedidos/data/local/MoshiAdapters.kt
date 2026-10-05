package com.alfonsokitoko.gestionpedidos.data.local

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MoshiDateAdapter {
	private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
		timeZone = TimeZone.getTimeZone("UTC")
	}

	@FromJson
	fun fromJson(json: String): Date? {
		return try {
			format.parse(json)
		} catch (e: Exception) {
			null
		}
	}

	@ToJson
	fun toJson(date: Date): String {
		return format.format(date)
	}
}

class MoshiCantidadAdapter {
	@FromJson
	fun fromJson(value: Double): Int {
		return value.toInt()
	}

	@ToJson
	fun toJson(value: Int): Double {
		return value.toDouble()
	}
}

class SkipSerializingAdapter {
	@ToJson
	fun toJson(@SkipSerializing value: String?): String? = null

	@FromJson
	@SkipSerializing
	fun fromJson(value: String?): String? = null
}