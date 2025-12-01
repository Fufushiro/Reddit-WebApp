# Sistema de Seguridad y Filtrado - Reddit WebApp

## 📋 Descripción

Se ha implementado un sistema completo de **filtrado y sanitización** para la aplicación Reddit WebApp que bloquea:

✅ **Scripts de rastreo** (Google Analytics, Facebook Pixel, etc.)
✅ **Anuncios y contenido promocional** (via CSS)
✅ **Iframes maliciosos** (solo acepta dominios confiables)
✅ **Atributos peligrosos** (onclick, data-*, jsaction, etc.)
✅ **Intentos de inyección de código** (vigilancia de mutaciones en DOM)

---

## 🏗️ Arquitectura de Componentes

### 1. **ContentSanitizer.kt**
Sanitiza HTML **antes de renderizarlo** en el WebView.

**Funciones:**
- Elimina etiquetas `<script>`, `<embed>`, `<object>`
- Filtra iframes no confiables
- Limpia atributos peligrosos (onclick, data-*, ng-*, jsaction)
- Agrega headers CSP (Content Security Policy)

**Cómo funciona:**
```
HTML sin procesar → ContentSanitizer → HTML limpio → WebView
```

**Agregar nuevos bloques:**
```kotlin
// En BLOCKED_TAG_PATTERNS
Regex("<video[^>]*>.*?</video>", RegexOption.DOT_MATCHES_ALL or RegexOption.IGNORE_CASE)
```

---

### 2. **ContentInterceptor.kt**
Bloquea solicitudes HTTP **antes de que se descarguen**.

**Funciones:**
- Bloquea dominios de rastreo conocidos
- Previene descarga de recursos innecesarios
- Registra intentos bloqueados

**Dominios bloqueados:**
- `google-analytics.com`, `googletagmanager.com`
- `facebook.com`, `doubleclick.net`
- `hotjar.com`, `mixpanel.com`
- Y más...

**Agregar nuevo dominio:**
```kotlin
// En BLOCKED_TRACKING_DOMAINS
"nuevo-rastreador.com"
```

---

### 3. **DOMStyleInjector.kt**
Inyecta CSS y JavaScript **después de que la página carga**.

**CSS - Oculta anuncios:**
```css
[data-testid*="ad"],
[data-testid*="sponsored"],
[class*="promoted"],
[class*="advertisement"] {
    display: none !important;
}
```

**JavaScript - Bloquea scripts dinámicos:**
- Intercepta `appendChild()` y `insertBefore()`
- Vigila cambios con `MutationObserver`
- Limpia atributos peligrosos en nuevos elementos

**Agregar nuevo selector CSS:**
```kotlin
// En BLOCKING_CSS, sección "BLOQUEADORES"
[class*="my-custom-ad"] {
    display: none !important;
}
```

---

### 4. **MainActivity.kt**
Punto de entrada que integra todos los componentes.

**Flujo de ejecución:**
1. WebView solicita página
2. ContentInterceptor bloquea rastreo
3. HTML se descarga
4. CSS se inyecta (oculta anuncios al instante)
5. JavaScript se inyecta (vigilancia dinámica)

---

## ⚠️ IMPORTANTE: Términos de Servicio

Este código **MODIFICA contenido entregado por Reddit**:

- ❌ **Viola ToS de Reddit**: Modificar/filtrar contenido
- ✅ **Uso personal**: Está permitido para uso personal local
- ⛔ **No distribuir**: No está aprobado para distribución comercial
- 🔒 **Privacidad**: Mejora privacidad pero reduce ingresos de Reddit

**Equivalente a:** usar adblocker en navegador + modo incógnito

**Consulta antes de usar en producción:**
- https://www.reddit.com/r/reddit.com/wiki/user_agreement

---

## 🚀 Instalación y Uso

### Requisitos
- Android Studio
- JDK 11+
- Gradle 8.0+

### Compilación

```bash
cd "Reddit WebApp"
./gradlew build
```

### Ejecución

```bash
./gradlew installDebug
```

O desde Android Studio: `Run > Run 'app'`

---

## 🔧 Cómo Agregar Nuevos Filtros

### **CASO 1: Bloquear nuevo dominio de rastreo**

**Archivo:** `ContentInterceptor.kt`

```kotlin
companion object {
    private val BLOCKED_TRACKING_DOMAINS = setOf(
        "google-analytics.com",
        // ... otros dominios ...
        "nuevo-rastreador.com"  // ← AGREGAR AQUÍ
    )
}
```

**Pasos:**
1. Identificar el dominio (usar DevTools del navegador)
2. Agregar a `BLOCKED_TRACKING_DOMAINS`
3. Compilar y probar

---

### **CASO 2: Ocultar nuevo tipo de anuncio**

**Archivo:** `DOMStyleInjector.kt`

Encuentra el selector CSS en DevTools:
```
// Abre DevTools en navegador de escritorio
// Inspecciona el anuncio
// Copia el selector
```

Agrega a `BLOCKING_CSS`:
```kotlin
val BLOCKING_CSS = """
    <style>
    /* ... estilos existentes ... */
    
    /* Nuevo anuncio a bloquear */
    .mi-clase-de-anuncio,
    [data-mi-atributo-de-ad] {
        display: none !important;
    }
    </style>
""".trimIndent()
```

**Importantísimo:** Always use `!important` to ensure override

---

### **CASO 3: Bloquear nuevo patrón de rastreo**

**Archivo:** `ContentSanitizer.kt`

```kotlin
companion object {
    private val BLOCKED_TAG_PATTERNS = listOf(
        // ... patrones existentes ...
        
        // Nuevo patrón
        Regex("<mi-etiqueta[^>]*>.*?</mi-etiqueta>", 
            RegexOption.DOT_MATCHES_ALL or RegexOption.IGNORE_CASE)
    )
}
```

---

## ✅ Testing y Validación

Después de cada cambio, verificar que:

```
✓ Reddit carga sin errores
✓ Feed principal es visible
✓ Puedo ver posts completos
✓ Puedo expandir comentarios
✓ Puedo hacer scroll sin lag
✓ Botones de upvote/downvote funcionan
✓ Puedo navegar entre subreddits
✓ Búsqueda funciona correctamente
✓ Videos cargan sin problemas
```

### Debugging

**Ver logs de seguridad:**
- Android Studio → Logcat → Filter: "SECURITY"
- Ver bloques por tipo: "INTERCEPTOR", "INJECTOR"

**Herramientas del navegador:**
```javascript
// En consola del navegador (si está disponible)
// Ver logs de eventos de seguridad
console.log('[SECURITY EVENT]')

// Ver observador de mutaciones activo
console.log('[SECURITY] DOM Security initialized')
```

---

## 🐛 Troubleshooting

### "Reddit no carga completamente"
**Causa:** Un filtro CSS bloqueó contenido esencial
**Solución:**
1. Comentar la regla CSS nueva
2. Recompilar y probar
3. Usar selectores más específicos

### "Los videos no juegan"
**Causa:** iframes de video bloqueados
**Solución:**
```kotlin
// En ContentSanitizer.kt
TRUSTED_IFRAME_DOMAINS = setOf(
    "reddit.com",
    "youtube.com",
    "v.redd.it"  // ← Asegurar que está aquí
)
```

### "Sigue apareciendo un anuncio"
**Causa:** Selector CSS incorrecto
**Solución:**
1. Usar DevTools en navegador de escritorio
2. Inspeccionar el elemento exacto
3. Copiar clase/id exacto
4. Probar selector en consola antes de agregarlo

### "Errores de CORS en consola"
**Esto es normal y esperado** cuando se bloquean solicitudes
- No afecta funcionalidad de Reddit
- Es silencioso en la mayoría de casos

---

## 📊 Componentes Detallados

### Flujo Completo de Seguridad

```
┌─────────────────────────────────────────────────────────┐
│ Usuario abre Reddit                                       │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
        ┌─────────────────────────┐
        │  ContentInterceptor     │
        │                         │
        │ ¿Dominio de rastreo?   │
        │ → SÍ: Bloquear          │
        │ → NO: Permitir          │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  HTML se descarga       │
        │  de Reddit              │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  ContentSanitizer       │
        │                         │
        │ - Remove <script>       │
        │ - Remove <iframe> bad   │
        │ - Remove on* attrs      │
        │ - Remove data-* attrs   │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  WebView renderiza HTML │
        │  LIMPIO                 │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  DOMStyleInjector       │
        │                         │
        │ 1. CSS:                 │
        │    Oculta anuncios      │
        │                         │
        │ 2. JavaScript:          │
        │    Vigila mutaciones    │
        │    Bloquea scripts dinám│
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  REDDIT LIMPIO          │
        │  SIN RASTREO            │
        │  SIN ANUNCIOS           │
        │  SIN SCRIPTS MALICIOSOS │
        └─────────────────────────┘
```

---

## 📝 Auditoría y Logs

Todos los eventos de seguridad se registran:

```
[SECURITY] Removed <script> element: https://google-analytics.com/...
[SECURITY] Removed onclick from <div>
[INTERCEPTOR] Blocked tracking: facebook.com/pixel.js
[INJECTOR] CSS security layer injected
[INJECTOR] JavaScript security layer injected
[SECURITY EVENT] blocked_script: https://doubleclick.net/...
```

**Para ver los logs:**
- Android Studio Logcat → Filter: `SECURITY`
- O buscar por `INTERCEPTOR`, `INJECTOR`

---

## 🛡️ Qué SE Protege

✅ Tu actividad NO se envía a Google Analytics
✅ Tu actividad NO se envía a Facebook
✅ Tus clics en anuncios NO se rastrean
✅ Tus datos NO se venden a brokers
✅ No se cargan publicidades innecesarias

---

## ❌ Qué NO se Protege

⚠️ Reddit aún ve tu IP
⚠️ Reddit sabe qué subreddits visitas
⚠️ Reddit ve qué votas/comentas
⚠️ Agente de usuario se sigue enviando

**Para más privacidad:** usa VPN + cuenta anónima

---

## 🔐 Consideraciones de Seguridad

1. **XSS Prevention**: Los scripts no se ejecutan
2. **CSRF Protection**: Se bloquean solicitudes a terceros
3. **Data Exfiltration**: Atributos de rastreo se limpian
4. **DOM Integrity**: MutationObserver vigila cambios

---

## 📚 Archivos Implementados

```
app/src/main/java/ia/ankherth/reddit/
├── MainActivity.kt              ← Punto de entrada
├── ContentSanitizer.kt          ← Sanitiza HTML
├── ContentInterceptor.kt        ← Bloquea rastreo HTTP
├── DOMStyleInjector.kt          ← Inyecta CSS/JS
└── SECURITY_POLICY.kt           ← Documentación
```

---

## 🚦 Estado de Compilación

✅ **Sin errores de compilación**
✅ **Dependencias resueltas**
✅ **Listo para ejecutar**

---

## 📞 Soporte y Debugging

**Para reportar problemas:**
1. Reproducir el problema
2. Ver Logcat en Android Studio
3. Buscar logs `[SECURITY]` o `[ERROR]`
4. Identificar qué componente causa problema
5. Comentar la regla problemática y probar

**Común encontrar:**
- Falsos positivos en filtros CSS
- Iframes legítimos bloqueados
- Contenido de Reddit que parece anuncio

---

## ✨ Ejemplo: Agregar Bloqueador de Imagen Tracking

**Problema:** Se carga `pixel.reddit.com/track.gif`

**Solución:**

**1. Usar ContentInterceptor:**
```kotlin
// ContentInterceptor.kt
BLOCKED_TRACKING_DOMAINS = setOf(
    ...,
    "pixel.reddit.com"
)
```

**2. O usar CSS en DOMStyleInjector:**
```css
img[src*="pixel"],
img[src*="tracking"] {
    display: none !important;
    width: 0 !important;
    height: 0 !important;
}
```

**3. Probar:**
- Compilar: `./gradlew build`
- Ejecutar: `./gradlew installDebug`
- Verificar que Reddit carga correctamente

---

## 📖 Referencias

- [Android WebView Documentation](https://developer.android.com/reference/android/webkit/WebView)
- [Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [Reddit Terms of Service](https://www.reddit.com/r/reddit.com/wiki/user_agreement)

---

**Nota Final:** Este proyecto es para **uso personal**. La modificación de contenido de terceros puede violar términos de servicio. Úsalo responsablemente. 🔒
