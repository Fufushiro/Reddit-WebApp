# Testing & Debugging - WebView Hardened

## 📋 Testing Checklist

### Antes de Deploy

- [ ] **Compilación:** `./gradlew build` completa sin errores
- [ ] **Pantalla Completa:** Status bar oculta en Android 11+
- [ ] **Swipe:** Swiping desde arriba muestra barra temporalmente
- [ ] **Carga:** Reddit carga en < 3 segundos
- [ ] **Feed:** Posts son visibles en el feed
- [ ] **Comentarios:** Se pueden expandir comentarios
- [ ] **Interacción:** Se puede upvote/downvote
- [ ] **Scroll:** No hay jank visible durante scroll rápido
- [ ] **ADs:** No se ven anuncios/contenido promocional
- [ ] **Performance:** No hay crashes o memory leaks

---

## 🔍 Debugging & Logs

### Activar Logcat

```bash
# Terminal 1: Device logs en tiempo real
adb logcat | grep -E "INTERCEPTOR|INJECTOR|SECURITY|PAGE"

# Terminal 2: Filtro solo de bloques de tracking
adb logcat | grep "INTERCEPTOR.*Blocked"

# Terminal 3: Todo desde la app
adb logcat | grep "ia.ankherth.reddit"
```

### Logs Esperados (ejemplos)

#### En startup:
```
[PAGE] Starting load: https://www.reddit.com
[INJECTOR] CSS blocking layer injected
[INJECTOR] Anti-jank CSS layer injected
[INJECTOR] JavaScript security layer injected
[PAGE] Finished load: https://www.reddit.com
[SECURITY] DOM Security initialized with anti-jank optimizations
```

#### Requests bloqueados:
```
[INTERCEPTOR] [tracking_domain] https://google-analytics.com/analytics.js
[INTERCEPTOR] [tracking_domain] https://tracking.reddit.com/pixel.gif
[INTERCEPTOR] [tracking_pattern] https://reddit.com/ads/banner
[INTERCEPTOR] [extension] https://fonts.google.com/montserrat.woff2
```

#### Scripts dinámicos bloqueados:
```
[SECURITY] Blocked dynamic script: https://googletagmanager.com/gtag/js
[SECURITY EVENT] blocked_script: https://hotjar.com/analytics.js
[SECURITY EVENT] removed_element: IFRAME
```

---

## 🎯 Test Cases Detallados

### Test 1: Pantalla Completa (Android 11+)

**Pasos:**
1. Abrir app en Android 11+
2. Observar que status bar está oculta

**Resultado esperado:**
- ✅ Status bar oculta
- ✅ Contenido ocupa pantalla completa

**Pasos adicionales:**
1. Swipear desde arriba hacia abajo
2. Status bar aparece momentáneamente
3. Tocar pantalla o esperar

**Resultado esperado:**
- ✅ Barra aparece después de swipe
- ✅ Se oculta automáticamente

### Test 2: Performance de Carga

**Pasos:**
1. Abrir app (cold start)
2. Medir tiempo hasta que feed es interactivo
3. Scroll hacia abajo
4. Click en un post
5. Medir tiempo de navegación atrás

**Métricas esperadas:**
- ⚡ Cold start: < 3 segundos
- ⚡ Navegación: < 1 segundo
- ⚡ Scroll: 60 FPS, sin jank

**Cómo medir:**
```kotlin
// En logcat, buscar logs de PAGE:
// [PAGE] Starting load: https://www.reddit.com (timestamp 1)
// [PAGE] Finished load: https://www.reddit.com (timestamp 2)
// Diferencia = tiempo de carga
```

### Test 3: Bloqueo de Tracking

**Pasos:**
1. Abrir logcat con filtro INTERCEPTOR
2. Cargar Reddit
3. Scroll en feed
4. Click en un post

**Resultado esperado:**
- ✅ Se bloquean dominios de google-analytics
- ✅ Se bloquean tracking.reddit.com requests
- ✅ Se bloquean URLs con /analytics, /pixel, etc.

**Ejemplo de logs esperados:**
```
[INTERCEPTOR] [tracking_domain] https://google-analytics.com/...
[INTERCEPTOR] [tracking_domain] https://tracking.reddit.com/...
[INTERCEPTOR] [tracking_pattern] https://example.com/analytics?...
```

### Test 4: Sin Anuncios

**Pasos:**
1. Cargar Reddit normalmente
2. Scroll en el feed principal
3. Buscar elementos promocionados

**Resultado esperado:**
- ✅ No se ven ads/anuncios
- ✅ No se ven posts "Promoted"
- ✅ No se ven banners publicitarios

**Nota:** Si ves anuncios, revisa logs de INJECTOR para verificar que se inyectó CSS

### Test 5: Scroll Performance (Anti-Jank)

**Pasos:**
1. Cargar Reddit
2. Hacer scroll rápido (fling)
3. Observar si hay saltos/stuttering
4. Repetir múltiples veces

**Resultado esperado:**
- ✅ Scroll fluido
- ✅ Sin visual stuttering
- ✅ Sin jank perceptible

**Debug:**
```javascript
// En DevTools (si tienes acceso):
// Chrome DevTools → Performance → Grabar → Scroll
// Buscar frames > 16ms (indica jank en 60 FPS)
```

### Test 6: User-Agent Cambiado

**Pasos:**
1. Abrir DevTools (si disponible)
2. O revisar Network requests en proxy

**Resultado esperado:**
```
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/91.0
```

**NOT:**
```
❌ Mozilla/5.0 (Linux; Android ...WebView
❌ Mozilla/5.0 (Linux; Android ...Chrome
```

---

## 🚨 Troubleshooting

### Problema: Reddit no carga

**Causa probable:** CSS/JS inyectado rompe renderizado

**Solución:**
1. Comentar `injectSecurityLayers()` en MainActivity.kt
2. Rebuild y probar si Reddit carga
3. Si carga, problema está en DOMStyleInjector
4. Comentar bloques CSS uno a uno hasta encontrar culpable

### Problema: Ads aún visibles

**Causa probable:** Selectores CSS no son lo suficientemente específicos

**Solución:**
1. Usar DevTools → Inspect element en el ad
2. Copiar clase/id/data-attribute
3. Agregar nuevo selector en BLOCKING_CSS
4. Ejemplo:
```css
/* Nuevo selector encontrado */
div[data-myad-format] {
    display: none !important;
}
```

### Problema: Scroll no es fluido

**Causa probable:** MutationObserver corre demasiado frecuente

**Solución:**
1. Ajustar timeout de scroll en JavaScript:
```javascript
scrollTimeout = setTimeout(function() {
    // Cambiar 150 a 250+ si sigue con jank
}, 150);
```

### Problema: Algunos botones no funcionan

**Causa probable:** JavaScript bloqueado agresivamente

**Solución:**
1. Revisar en DANGEROUS_ATTRIBUTES qué se está bloqueando
2. Ejemplo: si onclick se bloquea pero Reddit lo necesita:
```javascript
// Remover 'onclick' de DANGEROUS_ATTRIBUTES
// Usar más específico: 'data-onclick-malicious'
```

### Problema: Compilación falla

**Revisar:**
1. Sintaxis Kotlin correcta
2. Imports están agregados
3. Closing braces correctos

**Comando:**
```bash
./gradlew clean build
```

---

## 📊 Performance Profiling

### Con Android Studio Profiler

1. **Device → Memory:**
   - Esperar a que stabilice después de carga
   - Buscar leaks (memory que sube constantemente)

2. **Device → CPU:**
   - Medir durante scroll rápido
   - Buscar picos anormales

3. **Device → Network:**
   - Verificar que requests a tracking estén ausentes
   - Comparar con/sin Interceptor

### Con Logcat

```bash
# Ver requests HTTP (relativo)
adb logcat | grep -i "url\|request"

# Ver tiempo de carga
adb logcat | grep PAGE

# Ver eventos de seguridad
adb logcat | grep SECURITY
```

---

## 🔐 Verificación de Privacidad

### Dominios que DEBEN bloquearse:

```javascript
// Ejecutar en console de Chrome si abres Reddit en desktop
const trackingDomains = [
    'google-analytics.com',
    'googletagmanager.com',
    'tracking.reddit.com',
    'facebook.com',
    'doubleclick.net',
];

fetch('https://www.reddit.com')
    .then(r => r.text())
    .then(html => {
        trackingDomains.forEach(domain => {
            if (html.includes(domain)) {
                console.warn('⚠️ Found:', domain);
            }
        });
    });
```

---

## 📈 Checklist Final Antes de Release

- [ ] Build completa sin errores: `./gradlew build`
- [ ] APK generado: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Reddit carga en < 3 seg (cold start)
- [ ] Pantalla completa funciona (Android 11+)
- [ ] Scroll fluido (no hay jank)
- [ ] Sin ads visibles
- [ ] Logcat limpio (sin crashes)
- [ ] Upvote/downvote funciona
- [ ] Comentarios expandibles
- [ ] Búsqueda funciona
- [ ] Back button funciona
- [ ] Navegación lateral funciona

---

## 🎓 Recursos

### Android WebView API:
- https://developer.android.com/reference/android/webkit/WebView

### WindowInsetsController:
- https://developer.android.com/training/system-ui/immersive

### Performance Optimization:
- https://developer.android.com/topic/performance

### JavaScript in WebView:
- https://developer.android.com/guide/webapps/webview
