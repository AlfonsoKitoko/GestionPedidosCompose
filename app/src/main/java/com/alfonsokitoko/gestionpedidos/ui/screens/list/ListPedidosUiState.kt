package com.alfonsokitoko.gestionpedidos.ui.screens.list

import com.alfonsokitoko.gestionpedidos.data.model.Pedido

sealed interface ListPedidosUiState {
	object Loading : ListPedidosUiState
	data class Success(val pedidos: List<Pedido>) : ListPedidosUiState
	data class Error(val message: String) : ListPedidosUiState
}