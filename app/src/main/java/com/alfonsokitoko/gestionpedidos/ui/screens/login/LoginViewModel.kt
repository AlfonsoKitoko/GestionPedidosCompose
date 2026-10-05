package com.alfonsokitoko.gestionpedidos.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfonsokitoko.gestionpedidos.data.api.AuthApiService
import com.alfonsokitoko.gestionpedidos.data.model.LoginRequest
import com.alfonsokitoko.gestionpedidos.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
	private val authService: AuthApiService,
	private val sessionManager: SessionManager
) : ViewModel() {
	val loginTag = "LOGIN_CHECK"
	var email by mutableStateOf("")
	var password by mutableStateOf("")
	var isLoading by mutableStateOf(false)
	var errorMessage by mutableStateOf<String?>(null)
	var loginSuccess by mutableStateOf(false)

	init {
		if (sessionManager.getToken() != null) loginSuccess = true
	}

	fun onLoginClick() {
		if (email.isBlank() || password.isBlank()) {
			errorMessage = "Por favor, rellena todos los campos"
			return
		}
		viewModelScope.launch {
			isLoading = true
			errorMessage = null
			try {
				val request = LoginRequest(email = email, password = password)

				val response = authService.login(request)
				Log.d(loginTag, "Respuesta recibida: ${response.success}")

				if (response.success && response.data != null) {
					val token = response.data.token
					if (!token.isNullOrBlank()) {
						sessionManager.saveToken(token)
						loginSuccess = true
					} else {
						errorMessage = "Error de autenticación"
					}
				} else {
					errorMessage = "Credenciales incorrectas"
				}
			} catch (e: IOException) {
				Log.e(loginTag, "Fallo de conexión", e)
				errorMessage = "Error de red: ${e.localizedMessage}"
				if (sessionManager.getToken() != null) loginSuccess = true
				else errorMessage = "No hay conexión a Internet. Inténtelo más tarde."
			} catch (e: Exception) {
				Log.e(loginTag, "Error inesperado", e)
				errorMessage = "Ha ocurrido un error inesperado"
			} finally {
				isLoading = false
			}
		}
	}
}