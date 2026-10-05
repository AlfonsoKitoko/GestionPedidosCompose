package com.alfonsokitoko.gestionpedidos.ui.screens.list

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alfonsokitoko.gestionpedidos.data.model.FiltrosAvanzados
import com.alfonsokitoko.gestionpedidos.data.model.Pedido
import com.alfonsokitoko.gestionpedidos.ui.components.BaseLayout
import com.alfonsokitoko.gestionpedidos.ui.components.FiltrosDrawerContent
import com.alfonsokitoko.gestionpedidos.ui.components.PedidoCard
import com.alfonsokitoko.gestionpedidos.ui.components.SearchField
import com.alfonsokitoko.gestionpedidos.ui.components.SeccionFiltros
import com.alfonsokitoko.gestionpedidos.utils.CSVManager
import com.alfonsokitoko.gestionpedidos.utils.ExcelManager
import com.alfonsokitoko.gestionpedidos.utils.PedidoUtils
import com.alfonsokitoko.gestionpedidos.utils.enviarArchivo
import com.alfonsokitoko.gestionpedidos.utils.generateCSV
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPedidosScreen(
	viewModel: ListPedidosViewModel? = if (LocalInspectionMode.current) null else viewModel(),
	alQuererAgregar: () -> Unit,
	onPedidoClick: (Pedido) -> Unit,
	onLogout: () -> Unit
) {
	val context = LocalContext.current

	val excelManager = remember { ExcelManager(context) }

	val shareLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) { _ -> }

	val isOffline by viewModel?.isOffline?.collectAsStateWithLifecycle() ?: remember {
		mutableStateOf(
			false
		)
	}

	val importExcelLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent()
	) { uri: Uri? ->
		uri?.let {
			val pedidosActuales =
				(viewModel?.uiState?.value as? ListPedidosUiState.Success)?.pedidos ?: emptyList()
			if (pedidosActuales.isNotEmpty()) {
				excelManager.injectPendientes(uri, pedidosActuales)
			}
		}
	}

	val state by viewModel?.uiState?.collectAsStateWithLifecycle()
		?: remember { mutableStateOf(ListPedidosUiState.Loading) }
	val query by viewModel?.searchQuery?.collectAsStateWithLifecycle()
		?: remember { mutableStateOf("") }
	val filtrosActivos by viewModel?.filtrosActivos?.collectAsStateWithLifecycle()
		?: remember { mutableStateOf(emptySet()) }

	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	val filtrosAvanzados by viewModel!!.filtrosAvanzados.collectAsStateWithLifecycle()
	val proveedoresDisponibles = remember(state) {
		(state as? ListPedidosUiState.Success)?.pedidos?.map { it.proveedor }?.distinct() ?: emptyList()
	}

	val isRefreshing = viewModel?.isRefreshing ?: false

	var mostrarDialogoLogout by remember { mutableStateOf(false) }
	if (mostrarDialogoLogout) {
		AlertDialog(
			onDismissRequest = { mostrarDialogoLogout = false },
			title = { Text("Cerrar sesión") },
			text = { Text("¿Estás seguro de que quieres salir de la aplicación?") },
			confirmButton = {
				TextButton(
					onClick = {
						mostrarDialogoLogout = false
						viewModel?.logout { onLogout() }
					}
				) {
					Text("SALIR", color = MaterialTheme.colorScheme.error)
				}
			},
			dismissButton = {
				TextButton(onClick = { mostrarDialogoLogout = false }) {
					Text("CANCELAR")
				}
			}
		)
	}

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			FiltrosDrawerContent(
				filtros = filtrosAvanzados,
				proveedoresDisponibles = proveedoresDisponibles,
				onFiltrosCambiados = { viewModel?.actFiltrosAvanzados(it) },
				onLimpiar = { viewModel?.actFiltrosAvanzados(FiltrosAvanzados()) }
			)
		},
	) {
		Scaffold(
			floatingActionButton = {
				// Botón Agregar
				FloatingActionButton(onClick = alQuererAgregar) {
					Icon(Icons.Default.Add, contentDescription = "Agregar Pedido")
				}
			}
		) { innerPadding ->
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				Column(modifier = Modifier.fillMaxSize()) {
					AnimatedVisibility(
						visible = isOffline,
						enter = expandVertically(animationSpec = tween(500)) + fadeIn(),
						exit = shrinkVertically(animationSpec = tween(500)) + fadeOut()
					) {
						Surface(
							color = MaterialTheme.colorScheme.errorContainer,
							modifier = Modifier.fillMaxWidth()
						) {
							Row(
								modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Center
							) {
								Icon(
									imageVector = Icons.Default.CloudOff,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.onErrorContainer,
									modifier = Modifier.size(16.dp)
								)
								Spacer(Modifier.width(8.dp))
								Text(
									text = "Sin conexión con el servidor - Trabajando modo en local",
									style = MaterialTheme.typography.labelSmall,
									color = MaterialTheme.colorScheme.onErrorContainer
								)
							}
						}
					}
					BaseLayout(
						titulo = when (val s = state) {
							is ListPedidosUiState.Success -> "Pedidos (${s.pedidos.size})"
							else -> "Lista de Pedidos"
						},
						onBack = null,
						modifier = Modifier.weight(1f),
						actions = {
							// ++ BOTÓN EXPORTAR ++
							IconButton(onClick = {
								val lista = (state as? ListPedidosUiState.Success)?.pedidos ?: emptyList()
								if (lista.isNotEmpty()) {
									generateCSV(context, lista)?.let { archivo ->
										val intent = enviarArchivo(context, archivo)
										val chooser = Intent.createChooser(intent, "Exportar a Excel/CSV")
										shareLauncher.launch(chooser)
									}
								}
							}) {
								Icon(
									imageVector = Icons.Default.Share,
									contentDescription = "Exportar CSV",
									tint = MaterialTheme.colorScheme.primary
								)
							}
							// ++ BOTÓN IMPORTAR E INYECTAR ++
							val importExcelLauncher = rememberLauncherForActivityResult(
								contract = ActivityResultContracts.GetContent()
							) { uri: Uri? ->
								uri?.let {
									val lista = (state as? ListPedidosUiState.Success)?.pedidos ?: emptyList()
									// Llamamos al CSVManager para que compare y genere el archivo de novedades
									CSVManager(context).generarCsvNuevos(it, lista)
								}
							}

							IconButton(onClick = {
								// Abrimos el selector para buscar el Excel maestro (.xls)
								importExcelLauncher.launch("application/vnd.ms-excel")
							}) {
								Icon(
									imageVector = Icons.Default.VerticalAlignBottom,
									contentDescription = "Obtener novedades para Excel",
									tint = MaterialTheme.colorScheme.primary
								)
							}
							IconButton(onClick = {
								mostrarDialogoLogout = true
							}) {
								Icon(
									imageVector = Icons.AutoMirrored.Default.Logout,
									contentDescription = "Cerrar sesión",
									tint = MaterialTheme.colorScheme.error
								)
							}
						}
					) {
						Row(
							modifier = Modifier.padding(top = 8.dp, end = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							// +++ BUSCADOR +++
							Box(modifier = Modifier.weight(1f)) {
								SearchField(
									searchQuery = query,
									onSearchQueryChanged = { viewModel?.onSearchQueryChanged(it) }
								)
							}

							Box {
								IconButton(
									onClick = { scope.launch { drawerState.open() } }
								) {
									Icon(
										imageVector = Icons.Default.FilterList,
										contentDescription = "Filtros Avanzados",
									)
								}
							}
						}

						// +++ FILTRO ESTADO PEDIDO +++
						SeccionFiltros(
							filtrosSeleccionados = filtrosActivos,
							onFiltroPulsado = { filtro -> viewModel?.alternarFiltro(filtro) }
						)

						HorizontalDivider(
							modifier = Modifier.padding(horizontal = 16.dp),
							thickness = 1.dp,
							color = Color.LightGray.copy(alpha = 0.5f)
						)

						// +++ LISTA CON REFRESH +++
						PullToRefreshBox(
							isRefreshing = isRefreshing,
							onRefresh = { viewModel?.refresh() },
							modifier = Modifier.weight(1f)
						) {
							when (val s = state) {
								is ListPedidosUiState.Loading -> {
									Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
										CircularProgressIndicator()
									}
								}

								is ListPedidosUiState.Success -> {
									val lista = s.pedidos
									if (lista.isEmpty()) {
										Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
											Text("No hay pedidos con este filtro", color = Color.Gray)
										}
									} else {
										LazyColumn(
											modifier = Modifier.fillMaxSize(),
											contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
										) {
											items(lista, key = { it.id }) { pedido ->
												PedidoCard(pedido = pedido, onClick = { onPedidoClick(pedido) })
											}
										}
									}
								}

								is ListPedidosUiState.Error -> {
									Box(
										modifier = Modifier
											.fillMaxSize()
											.verticalScroll(rememberScrollState()), // Permite scroll para el pull-to-refresh
										contentAlignment = Alignment.Center
									) {
										Column(
											horizontalAlignment = Alignment.CenterHorizontally,
											modifier = Modifier.padding(32.dp)
										) {
											// Icono de error con el color rojo de tu PedidoUtils
											Icon(
												imageVector = Icons.Default.Warning,
												contentDescription = null,
												modifier = Modifier.size(64.dp),
												tint = PedidoUtils.rojoPtoVenir
											)

											Spacer(modifier = Modifier.height(16.dp))

											Text(
												text = "¡Ups! Algo salió mal",
												style = MaterialTheme.typography.titleMedium,
												fontWeight = FontWeight.Bold
											)

											Text(
												text = s.message, // El mensaje que viene del StateUI
												style = MaterialTheme.typography.bodyMedium,
												color = Color.Gray,
												textAlign = TextAlign.Center
											)

											Spacer(modifier = Modifier.height(24.dp))

											// Botón de reintento
											Button(
												onClick = { viewModel?.refresh() },
												colors = ButtonDefaults.buttonColors(
													containerColor = MaterialTheme.colorScheme.primary
												)
											) {
												Icon(
													Icons.Default.Refresh,
													contentDescription = null,
													modifier = Modifier.size(18.dp)
												)
												Spacer(modifier = Modifier.width(8.dp))
												Text("Reintentar conexión")
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}