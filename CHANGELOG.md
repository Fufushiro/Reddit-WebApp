# Changelog

Todos los cambios notables de este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [1.1] - 2024

### ✨ Agregado

#### Sistema de Seguridad de Tres Capas
- **ContentInterceptor.kt**: Nuevo componente que intercepta solicitudes HTTP/HTTPS antes de descargar
  - Bloquea dominios de rastreo conocidos (Google Analytics, Facebook Pixel, DoubleClick, etc.)
  - Bloquea extensiones de recursos no esenciales para reducir consumo de banda
  - Registra intentos de rastreo para auditoría
  - Más de 15 dominios de rastreo bloqueados por defecto

- **ContentSanitizer.kt**: Nuevo componente que sanitiza HTML antes de renderizar
  - Elimina etiquetas peligrosas: `<script>`, `<iframe>`, `<embed>`, `<object>`
  - Limpia atributos peligrosos: `onclick`, `data-*`, `ng-*`, `jsaction`
  - Filtra iframes no confiables (solo permite dominios verificados)
  - Agrega headers de Content Security Policy (CSP)
  - Soporte para dominios confiables: Reddit, YouTube, Imgur, etc.

- **DOMStyleInjector.kt**: Nuevo componente que inyecta protecciones en el DOM
  - CSS que oculta anuncios y contenido promocional
  - JavaScript que vigila cambios dinámicos en el DOM
  - Bloquea scripts que intentan cargarse después de la carga inicial
  - MutationObserver para detectar y eliminar elementos maliciosos
  - Limpieza automática de atributos de rastreo

- **SECURITY_POLICY.kt**: Documentación completa de políticas de seguridad
  - Explicación detallada de qué se filtra y por qué
  - Consideraciones legales sobre términos de servicio
  - Guía de extensión de filtros
  - Solución de problemas comunes

#### Integración en MainActivity
- Integración completa de los tres componentes de seguridad
- Inyección automática de CSS y JavaScript después de cargar páginas
- Logging mejorado para debugging y auditoría
- Manejo de errores robusto

#### Documentación
- **IMPLEMENTATION_GUIDE.md**: Guía completa de arquitectura e implementación
- **SECURITY_SUMMARY.md**: Resumen ejecutivo del sistema de seguridad
- **EXAMPLES.md**: Ejemplos prácticos de uso y extensión
- **TESTING_CHECKLIST.md**: Lista de verificación para testing
- **QUICK_START.md**: Guía de inicio rápido
- **INDEX.md**: Índice de documentación
- **DELIVERABLES.md**: Entregables del proyecto
- **FINAL_SUMMARY.md**: Resumen final del proyecto

### 🔄 Modificado

- **MainActivity.kt**: 
  - Refactorizado para integrar sistema de seguridad
  - Agregado `RedditWebViewClient` con inyección de capas de seguridad
  - Mejorado manejo de progreso de carga
  - Agregado logging detallado para debugging

- **build.gradle.kts**:
  - Actualizado `versionCode` a 2
  - Actualizado `versionName` a "1.1"
  - Actualizado `minSdk` a 31 (Android 12.0)
  - Actualizado `targetSdk` a 36
  - Actualizado `compileSdk` a 36

- **README.md**:
  - Actualizada descripción con explicación detallada de funcionalidad
  - Agregada sección sobre sistema de seguridad
  - Actualizados requisitos técnicos
  - Mejorada documentación de componentes

### 📝 Notas

- El sistema de seguridad está diseñado para uso personal
- Se recomienda revisar los términos de servicio de Reddit antes de usar
- Los filtros pueden afectar algunas funcionalidades de Reddit
- Todos los componentes incluyen logging para facilitar debugging

### ⚠️ Advertencias

- Modificar contenido de Reddit puede violar sus términos de servicio
- El bloqueo de rastreo reduce ingresos de Reddit por publicidad
- Uso recomendado solo para evaluación personal y privada
- No está aprobado para distribución comercial

---

## [1.0] - Versión Inicial

### ✨ Agregado

- Aplicación básica de WebView para Reddit
- Configuración de WebView con JavaScript habilitado
- Soporte para almacenamiento local (cookies, sesiones)
- Barra de progreso de carga
- Navegación con botón atrás
- Optimización de viewport para diferentes tamaños de pantalla
- Soporte de contenido mixto (HTTP/HTTPS)

### 📋 Características Iniciales

- Carga automática de `https://www.reddit.com`
- Interfaz limpia sin ActionBar
- Configuración básica de seguridad de WebView

---

## Formato de Versiones

- **MAYOR** (1.0.0): Cambios incompatibles con versiones anteriores
- **MENOR** (0.1.0): Nuevas funcionalidades compatibles hacia atrás
- **PARCHE** (0.0.1): Correcciones de errores compatibles hacia atrás

