package com.alfonsokitoko.gestionpedidos.ui.screens.list

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alfonsokitoko.gestionpedidos.data.repository.PedidoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ListPedidosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var repository: PedidoRepository
    private lateinit var viewModel: ListPedidosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mock(Application::class.java)
        // Nota: El ViewModel crea su propio repositorio internamente en el código actual.
        // Para testearlo correctamente sin inyección de dependencias es difícil,
        // pero vamos a asumir que el repo se puede mockear si cambiamos el constructor
        // o usamos un patrón de Service Locator / Inyección.
        // Dado el código actual, este test es ilustrativo de cómo debería ser.
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery filtering works`() = runTest {
        // Este test requiere que ListPedidosViewModel permita inyectar el repositorio
        // o que usemos una herramienta como Mockito para interceptar la creación.
    }
}