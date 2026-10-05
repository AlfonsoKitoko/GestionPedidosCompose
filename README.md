# GestionPedidos - Android App

![Android](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%28Material%203%29-brightgreen)

GestionPedidos es una aplicación nativa de Android desarrollada para la gestión eficiente de compras, proveedores y pedidos de producción. El proyecto está construido siguiendo las guías oficiales de arquitectura de Android y buenas prácticas de código limpio (Clean Architecture orientada a UI/Domain/Data).

---

## Tecnologías y Librerías Utilizadas

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose con Material Design 3 y Navigation Compose
- **Arquitectura:** MVVM (Model-View-ViewModel) + Corrutinas & Flow
- **Base de Datos Local:** Room Database con KSP (Kotlin Symbol Processing)
- **Red & JSON:** Retrofit 2 + Moshi Converter + OkHttp Logging Interceptor
- **Procesamiento de Documentos:** Apache POI (Excel / Scratchpad)
- **Tareas en Segundo Plano:** AndroidX WorkManager
- **Pruebas Unitarias e Instrumentadas:** JUnit 4, Mockito (Kotlin), Coroutines Test, Espresso & Compose UI Test

---

## Arquitectura del Proyecto

El proyecto sigue el patrón MVVM para desacoplar la lógica de negocio de la interfaz de usuario:

```text
com.alfonsokitoko.gestionpedidos/
├── data/           # Entidades Room, DAOs, API Services (Retrofit/Moshi) y Repositorios
├── domain/         # Casos de uso, Validadores y Modelos
├── ui/             # Composables (Screens, Navigation), ViewModels y Theme (M3)
└── utils/          # Gestores de archivos (ExcelManager / Apache POI) y helpers
```

## Características Clave

  * Persistencia Offline-First: Los datos se leen y almacenan localmente utilizando Room Database para garantizar el funcionamiento sin conexión.
  * Integración de Red: Peticiones HTTP a través de Retrofit deserializadas con Moshi.
  * Gestión de Hojas de Cálculo: Exportación y lectura de archivos Excel utilizando la librería Apache POI.
  * Validación de Formularios: Lógica desacoplada para la validaciones de entradas en tiempo real.
  * Diseño Adaptativo: Pantallas dinámicas creadas con componentes de Jetpack Compose y navegación nativa.
