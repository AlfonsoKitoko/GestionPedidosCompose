package com.alfonsokitoko.gestionpedidos.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ConnectivityObserver(context: Context) {
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	private fun getCurrentConnectivity(): Boolean {
		val network = connectivityManager.activeNetwork
		val capabilities = connectivityManager.getNetworkCapabilities(network)
		return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
	}

	val isConnected: Flow<Boolean> = callbackFlow {
		send(getCurrentConnectivity())

		val callback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				launch { send(true) }
			}

			override fun onLost(network: Network) {
				launch { send(false) }
			}

			override fun onUnavailable() {
				launch { send(false) }
			}
		}

		val request = NetworkRequest.Builder()
			.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.build()

		connectivityManager.registerNetworkCallback(request, callback)

		awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
	}.distinctUntilChanged()
}