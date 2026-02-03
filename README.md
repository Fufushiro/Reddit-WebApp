# Reddit WebApp

Reddit WebApp es un cliente Android que carga la versión web de Reddit dentro de un WebView, con capas de filtrado para bloquear rastreadores y anuncios, y con sanitización de HTML.

Versión: 1.3

Descripción breve:
- Carga `https://www.reddit.com` en un WebView
- Bloqueo de rastreo a nivel HTTP (ContentInterceptor)
- Sanitización de HTML (ContentSanitizer)
- Inyección de CSS/JS para ocultar anuncios y prevenir scripts dinámicos (DOMStyleInjector)

Requisitos:
- Android SDK 31+
- JDK 11+
- Android Studio (o Gradle CLI)

Compilar rápidamente:
1. Clona el repo
2. Desde la raíz del proyecto ejecuta:

```bash
./gradlew assembleDebug
```

Instalar en dispositivo/emulador:

```bash
./gradlew installDebug
```

Notas de seguridad:
- JavaScript está habilitado por funcionalidad de Reddit; esto puede aumentar el riesgo XSS. Revisa `MainActivity.kt` y `DOMStyleInjector.kt` si necesitas endurecer la política.
- Modificar o filtrar contenido puede afectar los Términos de Servicio de Reddit.

Licencia: MIT
