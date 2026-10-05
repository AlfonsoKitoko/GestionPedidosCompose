package com.alfonsokitoko.gestionpedidos.data.api

import android.content.Context
import android.content.Intent
import com.alfonsokitoko.gestionpedidos.MainActivity
import com.alfonsokitoko.gestionpedidos.utils.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {
	override fun authenticate(route: Route?, response: Response): Request? {
		if (response.priorResponse != null) return null

		val sessionManager = SessionManager(context)

		if (sessionManager.getToken() != null) sessionManager.logout()

		val intent = Intent(context, MainActivity::class.java).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		}
		context.startActivity(intent)

		return null
	}
}