# Reddit WebApp

Una aplicación Android nativa que proporciona acceso a Reddit a través de una interfaz web integrada.

## 📱 Descripción

Reddit WebApp es una aplicación móvil para Android que permite a los usuarios acceder a Reddit de manera optimizada en dispositivos móviles. La aplicación utiliza un WebView para cargar la versión web de Reddit, proporcionando una experiencia integrada y fluida.

## ✨ Características

- **Acceso a Reddit integrado**: Carga la versión web de Reddit directamente en la aplicación
- **JavaScript habilitado**: Soporte completo para funcionalidad interactiva de Reddit
- **Almacenamiento local**: Permite que Reddit almacene datos locales (cookies, sesiones, etc.)
- **Barra de progreso**: Indicador visual del progreso de carga de las páginas
- **Navegación intuitiva**: Botón atrás integrado para navegar entre páginas de Reddit
- **Optimización de pantalla**: Configuración automática para diferentes tamaños de pantalla
- **Soporte de contenido mixto**: Permite cargar contenido HTTP y HTTPS

## 🛠️ Requisitos Técnicos

- Android SDK 24 (Android 7.0) o superior
- Conexión a Internet
- AndroidX AppCompat
- Kotlin

## 📋 Permisos Requeridos

La aplicación utiliza los siguientes permisos:

- `android.permission.INTERNET` - Para acceder a Reddit en línea
- `android.permission.ACCESS_NETWORK_STATE` - Para verificar el estado de la conexión de red

## 🚀 Instalación y Compilación

### Requisitos previos

- Android Studio
- JDK 11 o superior
- Gradle 8.0 o superior

### Pasos de compilación

1. Clona el repositorio:
```bash
git clone https://github.com/Fufushiro/Reddit-WebApp.git
```

2. Abre el proyecto en Android Studio

3. Sincroniza Gradle:
```bash
./gradlew sync
```

4. Compila y ejecuta la aplicación:
```bash
./gradlew installDebug
```

O desde Android Studio: `Ejecutar > Ejecutar 'app'`

## 📁 Estructura del Proyecto

```
Reddit-WebApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ia/ankherth/reddit/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   ├── values/
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 💻 Componentes Principales

### MainActivity.kt
Actividad principal que:
- Configura el WebView para cargar Reddit
- Implementa un cliente WebView personalizado
- Maneja la progresión de carga
- Gestiona la navegación hacia atrás

### Configuración de WebView
- JavaScript habilitado para interactividad
- DOM Storage y Database habilitados
- Soporte para Vista Amplia (viewport)
- Modo de contenido mixto (HTTP/HTTPS)

## 🔧 Configuración

La aplicación carga automáticamente `https://www.reddit.com` al iniciarse. Las configuraciones principales incluyen:

```kotlin
webView.settings.apply {
    javaScriptEnabled = true        // Permite JavaScript
    domStorageEnabled = true        // Almacenamiento DOM
    databaseEnabled = true          // Base de datos local
    useWideViewPort = true          // Viewport optimizado
    loadWithOverviewMode = true     // Modo de vista general
    mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
}
```

## 🌐 URL de Inicio

Por defecto, la aplicación carga: `https://www.reddit.com`

Para cambiar la URL, modifica la línea en `MainActivity.kt`:
```kotlin
webView.loadUrl("https://www.reddit.com")
```

## 📝 Dependencias

El proyecto utiliza:
- AndroidX AppCompat
- Android WebKit
- Kotlin Standard Library

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Haz fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-caracteristica`)
3. Commit tus cambios (`git commit -am 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está disponible bajo la licencia MIT.

## 👨‍💻 Autor

**Fufushiro**

- GitHub: [@Fufushiro](https://github.com/Fufushiro)
- Repositorio: [Reddit-WebApp](https://github.com/Fufushiro/Reddit-WebApp)

## 🐛 Reporte de Errores

Si encuentras algún error o tienes sugerencias, por favor abre un issue en el repositorio de GitHub.

---

**Nota**: Esta aplicación es un cliente web para Reddit y depende de la disponibilidad y funcionalidad del sitio web de Reddit.
