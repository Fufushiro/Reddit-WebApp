# Reddit WebApp

**Versión actual: 1.3**

Una aplicación Android nativa que proporciona acceso a Reddit a través de una interfaz web integrada, **con un sistema avanzado de filtrado de rastreo, scripts maliciosos y anuncios**. Incluye optimizaciones de seguridad, mejor manejo de safe area, barra de estado, y protecciones contra fingerprinting de navegador.

## 📱 Descripción

Reddit WebApp es una aplicación móvil para Android que permite a los usuarios acceder a Reddit de manera optimizada en dispositivos móviles. La aplicación utiliza un WebView para cargar la versión web de Reddit, proporcionando una experiencia integrada y fluida.

### ¿Qué hace esta aplicación?

Esta aplicación es un **cliente web nativo para Reddit** con un **sistema avanzado de seguridad y privacidad** que funciona en tres niveles:

1. **Bloqueo de rastreo a nivel HTTP**: Intercepta y bloquea solicitudes a dominios de rastreo (Google Analytics, Facebook Pixel, etc.) antes de que se descarguen, reduciendo el consumo de datos y protegiendo tu privacidad.

2. **Sanitización de HTML**: Limpia el contenido HTML antes de renderizarlo, eliminando scripts maliciosos, iframes no confiables y atributos peligrosos que podrían comprometer tu seguridad.

3. **Protección del DOM**: Inyecta CSS y JavaScript después de que la página carga para ocultar anuncios y bloquear scripts que intenten cargarse dinámicamente, manteniendo una experiencia limpia y segura.

**Resultado**: Navegas Reddit con mayor privacidad, sin anuncios molestos y con protección contra scripts de rastreo de terceros.

## ✨ Características

### Funcionalidad Principal
- **Acceso a Reddit integrado**: Carga la versión web de Reddit directamente en la aplicación
- **JavaScript habilitado**: Soporte completo para funcionalidad interactiva de Reddit
- **Almacenamiento local**: Permite que Reddit almacene datos locales (cookies, sesiones, etc.)
- **Barra de progreso**: Indicador visual del progreso de carga de las páginas
- **Navegación intuitiva**: Botón atrás integrado para navegar entre páginas de Reddit
- **Optimización de pantalla**: Configuración automática para diferentes tamaños de pantalla
- **Soporte de contenido mixto**: Permite cargar contenido HTTP y HTTPS

### 🔒 Sistema de Seguridad y Filtrado (NUEVO)
- **Bloqueo de rastreo**: Intercepta solicitudes a dominios de rastreo (Google Analytics, Facebook, etc.)
- **Filtrado de scripts**: Elimina scripts maliciosos e intentos de inyección de código
- **Bloqueo de anuncios**: Oculta contenido promocional, patrocinado y publicitario
- **Sanitización de HTML**: Elimina iframes maliciosos, atributos peligrosos (onclick, data-*, etc.)
- **Vigilancia de DOM**: Monitorea cambios dinámicos y bloquea scripts que intentan cargarse después
- **Auditoría de seguridad**: Registra eventos de seguridad para debugging y análisis- **Protección de privacidad**: Reduce fingerprinting de navegador y exposición de APIs
## 🛠️ Requisitos Técnicos

- **Android SDK 31** (Android 12.0) o superior
- Conexión a Internet
- AndroidX AppCompat
- Kotlin
- JDK 11 o superior
- Gradle 8.0 o superior

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

## 🔒 Sistema de Seguridad y Filtrado

### Componentes de Seguridad

#### 1. **ContentInterceptor** - Bloqueo HTTP
- Intercepta solicitudes antes de descargar
- Bloquea dominios de rastreo conocidos
- Reduce consumo de banda

#### 2. **ContentSanitizer** - Limpieza de HTML
- Sanitiza HTML antes de renderizar
- Elimina `<script>`, `<iframe>`, `<embed>`, `<object>`
- Elimina atributos peligrosos (onclick, data-*, ng-*, jsaction)

#### 3. **DOMStyleInjector** - Protección de DOM
- Inyecta CSS que oculta anuncios
- Inyecta JavaScript que vigila cambios
- Bloquea scripts que intenten cargarse dinámicamente

### Dominios Bloqueados
- Google Analytics, Google Tag Manager
- Facebook Pixel, DoubleClick, Hotjar
- Y más de 15 dominios de rastreo conocidos

### Cómo Extender los Filtros
Ver documentación completa en: **`IMPLEMENTATION_GUIDE.md`**

## 📚 Documentación

### Documentación de Seguridad
- **`IMPLEMENTATION_GUIDE.md`** - Guía completa de arquitectura e implementación
- **`SECURITY_POLICY.kt`** - Políticas de seguridad y consideraciones legales
- **`SECURITY_SUMMARY.md`** - Resumen ejecutivo del sistema
- **`EXAMPLES.md`** - Ejemplos prácticos de uso y extensión

### Historial de Cambios
- **`CHANGELOG.md`** - Registro detallado de todos los cambios y versiones

## 📁 Estructura del Proyecto

```
Reddit-WebApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ia/ankherth/reddit/
```
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

### **MainActivity.kt**
Actividad principal que integra todos los componentes de seguridad:
- Inicializa ContentSanitizer y ContentInterceptor
- Configura el WebView con ajustes de seguridad
- Inyecta CSS y JavaScript de protección después de cargar
- Maneja progresión de carga y navegación hacia atrás

### **ContentSanitizer.kt**
Sanitiza HTML antes de renderizarlo (~130 líneas):
- Elimina etiquetas prohibidas (script, embed, object)
- Limpia atributos peligrosos (onclick, data-*, ng-*)
- Filtra iframes no confiables
- Agrega headers de Content Security Policy

### **ContentInterceptor.kt**
Bloquea solicitudes HTTP sospechosas (~100 líneas):
- Intercepta requests a dominios de rastreo
- Previene descarga de recursos innecesarios
- Registra intentos de seguridad

### **DOMStyleInjector.kt**
Inyecta protecciones después de renderizar (~250 líneas):
- CSS que oculta anuncios y elementos promocionales
- JavaScript que vigila cambios en DOM
- Bloquea scripts que intenten cargarse dinámicamente

### Configuración de WebView
- JavaScript habilitado para interactividad
- DOM Storage y Database habilitados
- Soporte para Vista Amplia (viewport)
- Modo de contenido mixto (HTTP/HTTPS)
- Protecciones de seguridad adicionales

## 🔧 Configuración

La aplicación carga automáticamente `https://www.reddit.com` al iniciarse con el siguiente flujo:

```
1. WebView solicita página
    ↓
2. ContentInterceptor bloquea rastreo
    ↓
3. HTML se descarga
    ↓
4. ContentSanitizer limpia HTML
    ↓
5. WebView renderiza HTML limpio
    ↓
6. DOMStyleInjector inyecta CSS + JS
    ↓
7. Usuario ve Reddit sin rastreo/anuncios
```

Las configuraciones de seguridad principales son:

```kotlin
```
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


### Evaluación de ToS
- Modificar contenido: Puede violar ToS de Reddit
- Bloquear rastreo: Equivalente a adblocker en navegador
- Privacidad: Mejora privacidad del usuario pero reduce ingresos de Reddit

**Recomendación:** Revisar Términos de Servicio antes de uso en producción:
- https://www.reddit.com/r/reddit.com/wiki/user_agreement

### Impacto en Reddit
- Reduce ingresos por publicidad (anuncios no vistos)
- Reduce datos de analytics
- Reduce perfilado de usuarios

## 🐛 Reporte de Errores

Si encuentras algún error o tienes sugerencias, por favor abre un issue en el repositorio de GitHub.

---

**Nota**: Esta aplicación es un cliente web para Reddit y depende de la disponibilidad y funcionalidad del sitio web de Reddit.

**Seguridad**: El sistema de filtrado se proporciona como está. Úsalo responsablemente y respeta los términos de servicio.

```
