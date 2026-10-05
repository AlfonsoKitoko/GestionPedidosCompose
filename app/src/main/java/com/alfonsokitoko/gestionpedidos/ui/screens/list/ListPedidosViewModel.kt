package com.alfonsokitoko.gestionpedidos.ui.screens.list

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alfonsokitoko.gestionpedidos.data.api.RetrofitClient
import com.alfonsokitoko.gestionpedidos.data.model.FiltroEstado
import com.alfonsokitoko.gestionpedidos.data.model.FiltrosAvanzados
import com.alfonsokitoko.gestionpedidos.data.repository.ConnectivityObserver
import com.alfonsokitoko.gestionpedidos.data.repository.PedidoRepository
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils
import com.alfonsokitoko.gestionpedidos.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar


class ListPedidosViewModel(
	application: Application,
	private val sessionManager: SessionManager,
) : AndroidViewModel(application) {
	private val connectivityObserver = ConnectivityObserver(application)
	private val repository = PedidoRepository(application)
	val searchQuery = MutableStateFlow("")

	// Cambiamos el Map por un solo estado
	val filtrosActivos = MutableStateFlow<Set<FiltroEstado>>(emptySet())
	val ordenActual = MutableStateFlow<String>("")
	val filtrosAvanzados = MutableStateFlow(FiltrosAvanzados())
	var isRefreshing by mutableStateOf(false)
		private set
	private val _isBackendAvailable = MutableStateFlow(true)
	val isBackendAvailable: StateFlow<Boolean> = _isBackendAvailable

	val isOffline: StateFlow<Boolean> = combine(
		connectivityObserver.isConnected,
		_isBackendAvailable
	) { conectado, servidorVivo ->
		!conectado || !servidorVivo
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Eagerly,
		initialValue = false
	)

	init {
		refresh()

		viewModelScope.launch {
			connectivityObserver.isConnected.collect { connected ->
				if (connected) {
					repository.enqueueSyncWork()
					refresh()
				} else {
					_isBackendAvailable.value = false
				}
			}
		}
	}

	fun refresh() {
		viewModelScope.launch {
			isRefreshing = true
			try {
				repository.refreshPedidos()
				// Si la llamada al repo tiene éxito, el servidor está VIVO
				_isBackendAvailable.value = true
				Log.d("API_CHECK", "Servidor conectado con éxito")
			} catch (e: Exception) {
				// Si hay un error de red (Timeout, 404, 500, NoRouteToHost...)
				Log.e("API_CHECK", "Servidor inalcanzable: ${e.message}")
				_isBackendAvailable.value = false
			} finally {
				isRefreshing = false
			}
		}
	}

	fun logout(onNavigateToLogin: () -> Unit) {
		viewModelScope.launch {
			// Borra el token cifrado de las SharedPreferences
			sessionManager.logout()
			// Limpia el caché de Retrofit (por si acaso)
			RetrofitClient.reset()
			// Callback para navegar a Login
			onNavigateToLogin()
		}
	}

	// EL ESTADO PRINCIPAL DE LA UI
	val uiState: StateFlow<ListPedidosUiState> = combine(
		repository.allPedidos,
		searchQuery,
		filtrosActivos,
		ordenActual,
		filtrosAvanzados
	) { pedidos, query, filtros, orden, avanzados ->
		val filtrados = pedidos.filter { pedido ->
			// 1. Filtro de búsqueda
			val matchesQuery = query.isBlank() ||
					pedido.proveedor.contains(query, ignoreCase = true) ||
					pedido.descripcion.contains(query, ignoreCase = true)

			// 2. Filtro de múltiples estados
			val matchesEstado = if (filtros.isEmpty()) {
				true
			} else {
				filtros.any { filtro ->
					when (filtro) {
						FiltroEstado.ROJO -> pedido.estadoCalculado == PedidoUtils.rojoPtoVenir
						FiltroEstado.VERDE -> pedido.estadoCalculado == PedidoUtils.verdeRecibido
						FiltroEstado.AZUL -> pedido.estadoCalculado == PedidoUtils.azulDocumentado
					}
				}
			}
			val matchesProveedor =
				avanzados.proveedor == null || pedido.proveedor == avanzados.proveedor
			val cal = Calendar.getInstance().apply { time = pedido.meses }
			val matchesMonth = avanzados.month == null || cal.get(Calendar.MONTH) == avanzados.month

			matchesQuery && matchesEstado && matchesProveedor && matchesMonth
		}

		ListPedidosUiState.Success(filtrados)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = ListPedidosUiState.Loading
	)

	// --- NUEVAS FUNCIONES ---

	fun alternarFiltro(filtro: FiltroEstado) {
		val selectActual = filtrosActivos.value

		val nuevoSelect = if (selectActual.contains(filtro)) {
			selectActual - filtro
		} else {
			selectActual + filtro
		}

		if (nuevoSelect.size >= 3 || nuevoSelect.isEmpty())
			filtrosActivos.value = emptySet()
		else
			filtrosActivos.value = nuevoSelect
	}

	fun actFiltrosAvanzados(nuevos: FiltrosAvanzados) {
		filtrosAvanzados.value = nuevos
	}

	fun onSearchQueryChanged(newQuery: String) {
		searchQuery.value = newQuery
	}
}