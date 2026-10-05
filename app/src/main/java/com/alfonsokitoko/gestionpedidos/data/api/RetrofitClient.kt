package com.alfonsokitoko.gestionpedidos.data.api

import android.content.Context
import android.os.Build
import com.alfonsokitoko.gestionpedidos.data.local.MoshiCantidadAdapter
import com.alfonsokitoko.gestionpedidos.data.local.MoshiDateAdapter
import com.alfonsokitoko.gestionpedidos.data.local.SkipSerializingAdapter
import com.alfonsokitoko.gestionpedidos.utils.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
	// URL de la API
	private const val BASE_URL = "http://127.0.0.1:3010/"
	private var authService: AuthApiService? = null
	private var pedidoService: PedidoApiService? = null
	private var okHttpClient: OkHttpClient? = null

	fun reset() {
		authService = null
		pedidoService = null
		okHttpClient = null
	}

	// Parser para el JSON
	val moshi: Moshi = Moshi.Builder()
		.add(SkipSerializingAdapter())
		.add(MoshiCantidadAdapter())
		.add(MoshiDateAdapter())
		.addLast(KotlinJsonAdapterFactory())
		.build()

	private val loggingInterceptor = HttpLoggingInterceptor().apply {
		level = HttpLoggingInterceptor.Level.BASIC
	}

	private fun getOkHttpClient(context: Context): OkHttpClient {
		return okHttpClient ?: synchronized(this) {
			okHttpClient ?: createOkHttpClient(context).also { okHttpClient = it }
		}
	}

	private fun createOkHttpClient(context: Context): OkHttpClient {
		val sessionManager = SessionManager(context.applicationContext)
		return OkHttpClient.Builder()
			.connectTimeout(5, TimeUnit.SECONDS)
			.readTimeout(5, TimeUnit.SECONDS)
			.writeTimeout(5, TimeUnit.SECONDS)
			.retryOnConnectionFailure(false)
			.authenticator(TokenAuthenticator(context))
			.addInterceptor { chain ->
				val token = sessionManager.getToken()

				android.util.Log.d(
					"CAPTURA_TOKEN",
					"1. ¿Hay token en SessionManager?: ${if (token != null) "SÍ" else "NO"}"
				)
				if (token != null) {
					android.util.Log.d("CAPTURA_TOKEN", "2. Longitud del token: ${token.length}")
				}

				// Debug log para confirmar que el interceptor se despierta
				android.util.Log.d("RETROFIT_DEBUG", "Token en interceptor: ${token?.take(10)}...")

				val requestBuilder = chain.request().newBuilder()
					.addHeader("x-device-name", "${Build.MANUFACTURER} ${Build.MODEL}")
					.addHeader("x-android-version", Build.VERSION.RELEASE)
					.addHeader("Accept", "application/json")

				token?.let {
					android.util.Log.d("CAPTURA_TOKEN", "3. Aplicando Header Authorization...")
					requestBuilder.addHeader("Authorization", "Bearer $it")
				}
				chain.proceed(requestBuilder.build())
			}
			.addInterceptor(loggingInterceptor)
			.build()
	}

	fun getAuthService(context: Context): AuthApiService {
		return authService ?: synchronized(this) {
			authService ?: buildRetrofit(context).create(AuthApiService::class.java)
				.also { authService = it }
		}
	}

	fun getPedidoService(context: Context): PedidoApiService {
		return pedidoService ?: synchronized(this) {
			pedidoService ?: buildRetrofit(context).create(PedidoApiService::class.java)
				.also { pedidoService = it }
		}
	}

	private fun buildRetrofit(context: Context): Retrofit {
		return Retrofit.Builder()
			.baseUrl(BASE_URL)
			.client(getOkHttpClient(context))
			.addConverterFactory(MoshiConverterFactory.create(moshi))
			.build()
	}
}