package com.alfonsokitoko.gestionpedidos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alfonsokitoko.gestionpedidos.data.api.RetrofitClient
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.ui.screens.add.AltaPedidoScreen
import com.alfonsokitoko.gestionpedidos.ui.screens.detail.DetailPedidoScreen
import com.alfonsokitoko.gestionpedidos.ui.screens.edit.EditPedidoScreen
import com.alfonsokitoko.gestionpedidos.ui.screens.list.ListPedidosViewModel
import com.alfonsokitoko.gestionpedidos.ui.screens.list.ListaPedidosScreen
import com.alfonsokitoko.gestionpedidos.ui.screens.login.LoginScreen
import com.alfonsokitoko.gestionpedidos.ui.screens.login.LoginViewModel
import com.alfonsokitoko.gestionpedidos.ui.theme.GestionPedidosTheme
import com.alfonsokitoko.gestionpedidos.utils.SessionManager

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()

		val sessionManager = SessionManager(this)

		setContent {
			GestionPedidosTheme {
				val navController = rememberNavController()
				val context = LocalContext.current

				var pedidoSeleccionado by remember { mutableStateOf<Pedido?>(null) }

				val startDest = if (sessionManager.getToken() != null) "lista" else "login"

				NavHost(navController = navController, startDestination = startDest) {

					composable("login") {
						val authService = RetrofitClient.getAuthService(context)
						val loginViewModel = remember { LoginViewModel(authService, sessionManager) }
						LoginScreen(
							viewModel = loginViewModel,
							onNavigateToMain = {
								RetrofitClient.reset()
								navController.navigate("lista") {
									popUpTo("login") { inclusive = true }
								}
							}
						)
					}

					composable("lista") {
						val listViewModel: ListPedidosViewModel = viewModel(
							factory = object : ViewModelProvider.Factory {
								override fun <T : ViewModel> create(modelClass: Class<T>): T {
									return ListPedidosViewModel(application, sessionManager) as T
								}
							}
						)

						ListaPedidosScreen(
							viewModel = listViewModel,
							alQuererAgregar = { navController.navigate("alta") },
							onPedidoClick = { pedido ->
								pedidoSeleccionado = pedido
								navController.navigate("detalle")
							},
							onLogout = {
								navController.navigate("login") {
									popUpTo(0) { inclusive = true }
								}
							}
						)
					}

					composable("alta") {
						AltaPedidoScreen(
							alFinalizar = {
								navController.popBackStack()
							}
						)
					}

					composable("detalle") {
						key(pedidoSeleccionado) {
							pedidoSeleccionado?.let { pedido ->
								DetailPedidoScreen(
									pedido = pedido,
									onVolver = { navController.popBackStack() },
									onEditarClick = { navController.navigate("editar") },
									onBorradoExitoso = {
										pedidoSeleccionado = null
										navController.popBackStack()
									}
								)
							} ?: LaunchedEffect(Unit) {
								navController.popBackStack()
							}
						}
					}

					composable("editar") {
						pedidoSeleccionado?.let { pedido ->
							EditPedidoScreen(
								pedidoOld = pedido,
								alFinalizar = { pedidoUpdated ->
									pedidoSeleccionado = pedidoUpdated
									navController.popBackStack()
								},
								onCancelar = { navController.popBackStack() }
							)
						} ?: LaunchedEffect(Unit) { navController.popBackStack() }
					}
				}
			}
		}
	}
}