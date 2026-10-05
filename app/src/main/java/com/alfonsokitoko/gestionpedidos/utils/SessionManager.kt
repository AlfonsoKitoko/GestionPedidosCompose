package com.alfonsokitoko.gestionpedidos.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SessionManager(context: Context) {
	private val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
	private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
	private val KEY_ALIAS = "session_token_key"
	private val TRANSFORMATION = "AES/GCM/NoPadding"

	init {
		if (!keyStore.containsAlias(KEY_ALIAS)) {
			val keyGenerator =
				KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
			val spec = KeyGenParameterSpec.Builder(
				KEY_ALIAS,
				KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
			)
				.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
				.setKeySize(256)
				.build()
			keyGenerator.init(spec)
			keyGenerator.generateKey()
		}
	}

	fun saveToken(token: String) {
		try {
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
			val encryptedBytes = cipher.doFinal(token.toByteArray())
			val combined = cipher.iv + encryptedBytes // IV (12 bytes) + Datos

			// USAMOS NO_WRAP para evitar saltos de línea que rompen el token
			val base64Token = Base64.encodeToString(combined, Base64.NO_WRAP)

			sharedPrefs.edit().putString("encrypted_token", base64Token)
				.commit() // COMMIT es más seguro aquí
			android.util.Log.d(
				"SESSION_DEBUG",
				"¡TOKEN GUARDADO CON ÉXITO! -> ${base64Token.take(10)}..."
			)
		} catch (e: Exception) {
			android.util.Log.e("SESSION_DEBUG", "Error al guardar: ${e.message}")
		}
	}

	/*fun saveToken(token: String) {
		Log.d("SESSION_DEBUG", "Intentando guardar token...")
		sharedPrefs.edit().putString("encrypted_token", "TOKEN_DE_PRUEBA").commit()
		Log.d("SESSION_DEBUG", "Guardado manual finalizado")
	}*/

	fun getToken(): String? {
		val encryptedData = sharedPrefs.getString("encrypted_token", null) ?: return null
		return try {
			val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)

			// GCM usa 12 bytes para el IV
			val iv = decoded.copyOfRange(0, 12)
			val encryptedBytes = decoded.copyOfRange(12, decoded.size)

			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))

			val decrypted = String(cipher.doFinal(encryptedBytes))
			android.util.Log.d("SESSION_DEBUG", "¡TOKEN RECUPERADO Y DESCIFRADO!")
			decrypted
		} catch (e: Exception) {
			android.util.Log.e("SESSION_DEBUG", "Error al descifrar: ${e.message}")
			null
		}
	}

	private fun getSecretKey(): SecretKey {
		return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
	}

	fun logout() = sharedPrefs.edit().remove("encrypted_token").commit()
}