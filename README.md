# GestionPedidosCompose - Android App

GestionPedidosCompose es una aplicación nativa de Android desarrollada para la gestión eficiente de compras, proveedores y pedidos de producción. El proyecto está construido siguiendo las guías oficiales de arquitectura de Android y buenas prácticas de código limpio (Clean Architecture orientada a UI/Domain/Data).

## Tecnologías y Librerías Utilizadas

* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose con Material Design 3
* **Arquitectura:** MVVM (Model-View-ViewModel) + StateFlow / LiveData
* **Base de Datos Local:** Room Database con Corrutinas y Flow
* **Red y API:** Retrofit 2 + Moshi para la deserialización JSON
* **Asincronía:** Kotlin Coroutines & Flow
* **Inyección de Dependencias / Estado:** ViewModel & Repository Pattern
* **Pruebas Unitarias:** JUnit 4 & MockK / AndroidX Test

## Arquitectura del Proyecto

El proyecto sigue el patrón MVVM para desacoplar la lógica de negocio de la interfaz de usuario:

```
com.alfonsokitoko.gestionpedidos/
├── data/           # Entidades Room, DAOs, API Services y Repositorios
├── domain/         # Casos de uso, Validadores (e.g. PedidoValidator) y Modelos
├── ui/             # Composables (Screens, Components), ViewModels y Theme (M3)
└── utils/          # Gestores de archivos (ExcelManager), exportación y helpers
```

### Características Clave

* **Persistencia Offline-First:** Los datos se leen y almacenan localmente utilizando Room Database para garantizar el funcionamiento sin conexión.
* **Validación de Formularios:** Lógica desacoplada para la validación reactiva de campos de entrada en tiempo real.
* **Exportación de Datos:** Funcionalidad para la generación y compartición de informes/pedidos a través de gestores locales.
* **Diseño Adaptativo:** Pantallas dinámicas creadas con componentes reutilizables en Jetpack Compose.
