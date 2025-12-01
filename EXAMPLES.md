# 📖 Ejemplos Prácticos - Sistema de Filtrado

Este documento contiene ejemplos reales de cómo usar y extender el sistema de seguridad.

---

## 1. Bloquear un Nuevo Dominio de Rastreo

### Escenario
Descubres que Facebook está rastreando tus acciones. Quieres bloquearlo.

### Paso 1: Identificar el dominio
```javascript
// En navegador, abre DevTools (F12)
// Network tab → busca "facebook"
// Ves que se carga: https://graph.facebook.com/...
```

### Paso 2: Agregar a ContentInterceptor
```kotlin
// Archivo: ContentInterceptor.kt
companion object {
    private val BLOCKED_TRACKING_DOMAINS = setOf(
        "google-analytics.com",
        "googletagmanager.com",
        "facebook.com",
        "fbcdn.net",
        "doubleclick.net",
        // ... otros ...
        "graph.facebook.com"  // ← AGREGAR AQUÍ (nuevo)
    )
}
```

### Paso 3: Compilar y probar
```bash
./gradlew build
./gradlew installDebug

# Ver logs
adb logcat | grep "graph.facebook"
```

### Resultado esperado
```
[INTERCEPTOR] Blocked tracking: https://graph.facebook.com/tracking
```

---

## 2. Ocultar un Nuevo Tipo de Anuncio

### Escenario
Reddit muestra un nuevo tipo de anuncio con clase `.reddit-promoted-new`. Quieres ocultarlo.

### Paso 1: Identificar el selector
```javascript
// En navegador, abre DevTools (F12)
// Inspecciona el anuncio
// Ves que tiene: class="reddit-promoted-new"
```

### Paso 2: Agregar a DOMStyleInjector
```kotlin
// Archivo: DOMStyleInjector.kt
val BLOCKING_CSS = """
    <style>
    /* ... estilos existentes ... */
    
    /* NUEVOS BLOQUEADORES */
    .reddit-promoted-new,
    [class*="promoted-new"] {
        display: none !important;
    }
    </style>
""".trimIndent()
```

### Paso 3: Compilar y probar
```bash
./gradlew build
./gradlew installDebug

# Ver en logcat
adb logcat | grep INJECTOR
```

### Verificación
- Abre Reddit
- El anuncio debe estar oculto
- Otros contenido debe ser visible

---

## 3. Filtrar un Atributo de Rastreo Nuevo

### Escenario
Reddit introduce un nuevo atributo `data-user-id` para rastreo. Quieres eliminarlo.

### Paso 1: Agregar patrón a ContentSanitizer
```kotlin
// Archivo: ContentSanitizer.kt
companion object {
    private val DANGEROUS_ATTRIBUTE_PATTERNS = listOf(
        Regex("\\s+on\\w+\\s*=", RegexOption.IGNORE_CASE),  // onclick, onload, etc.
        Regex("\\s+data-\\S*\\s*=", RegexOption.IGNORE_CASE),  // data-* (incluye data-user-id)
        Regex("\\s+jsaction\\s*=", RegexOption.IGNORE_CASE),  // jsaction
        Regex("\\s+ng-\\S*\\s*=", RegexOption.IGNORE_CASE)  // ng-*
        // El patrón data-* ya cubre data-user-id
    )
}
```

### Paso 2: Probar
```bash
./gradlew build
./gradlew installDebug
```

### Logs esperados
```
[SECURITY] Removed data-user-id from <div>
[SECURITY] Removed data-user-id from <span>
```

---

## 4. Permitir un iframe de Confianza

### Escenario
YouTube no carga. Descubres que su iframe está siendo bloqueado.

### Paso 1: Verificar que youtube.com está en whitelist
```kotlin
// Archivo: ContentSanitizer.kt
companion object {
    private val TRUSTED_IFRAME_DOMAINS = setOf(
        "reddit.com",
        "redditmedia.com",
        "redditcdn.com",
        "i.redd.it",
        "v.redd.it",
        "imgur.com",
        "youtube.com",  // ← Debe estar aquí
        "youtu.be"
    )
}
```

### Paso 2: Si no está, agregarlo
```kotlin
private val TRUSTED_IFRAME_DOMAINS = setOf(
    // ... otros dominios ...
    "mi-dominio-confiable.com"  // ← AGREGAR
)
```

### Paso 3: Compilar y probar
```bash
./gradlew build
./gradlew installDebug
```

---

## 5. Bloquear un Script Específico

### Escenario
Un script malicioso se intenta cargar desde `malicious.com`. Quieres bloquearlo.

### Opción A: Por dominio
```kotlin
// ContentInterceptor.kt
private val BLOCKED_TRACKING_DOMAINS = setOf(
    // ...
    "malicious.com"  // ← Bloqueará todos los scripts de aquí
)
```

### Opción B: Por patrón
```kotlin
// DOMStyleInjector.kt
const BLOCKED_SCRIPT_PATTERNS = [
    'malicious',
    'evil-script',
    // ...
]
```

### Opción C: Por HTML
```kotlin
// ContentSanitizer.kt
private val BLOCKED_TAG_PATTERNS = listOf(
    Regex("<script[^>]*src=['\"].*?malicious[^'\"]*['\"]", setOf(RegexOption.IGNORE_CASE))
)
```

---

## 6. Crear Regla Personalizada Compleja

### Escenario
Reddit usa div con id `ad-container-v2` para anuncios. Los selectores simples no funcionan.

### Paso 1: Crear regla CSS específica
```kotlin
// DOMStyleInjector.kt
val BLOCKING_CSS = """
    <style>
    /* Ocultar contenedor de anuncios v2 y sus hijos */
    #ad-container-v2,
    #ad-container-v2 * {
        display: none !important;
        visibility: hidden !important;
        position: absolute !important;
        left: -9999px !important;
    }
    
    /* Asegurar que contenido válido no se vea afectado */
    article[data-testid*="post"] {
        display: block !important;
        visibility: visible !important;
        position: static !important;
    }
    </style>
""".trimIndent()
```

### Paso 2: Combinar con JavaScript si es necesario
```javascript
// En BLOCKING_JAVASCRIPT
const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        mutation.addedNodes.forEach(function(node) {
            // Remover específicamente elementos con id ad-container-v2
            if (node.id === 'ad-container-v2') {
                node.remove();
                logSecurityEvent('removed_specific_ad', 'ad-container-v2');
            }
        });
    });
});
```

---

## 7. Caso: El Anuncio No Se Oculta (Debugging)

### Problema
Agregaste una regla CSS pero el anuncio sigue apareciendo.

### Solución paso a paso

**Paso 1: Verificar que se está inyectando CSS**
```bash
adb logcat | grep "CSS security"
# Debe mostrar: [INJECTOR] CSS security layer injected
```

**Paso 2: Verificar que el selector es correcto**
```javascript
// En DevTools del navegador
// Abre la consola y ejecuta:
document.querySelectorAll('.mi-clase-de-anuncio').length
// Si retorna 0, el selector es incorrecto
```

**Paso 3: Ajustar el selector**
```kotlin
// Si el selector no funciona, prueba variaciones:

// Original
.mi-clase-de-anuncio { display: none !important; }

// Variación 1: más específico
div.mi-clase-de-anuncio { display: none !important; }

// Variación 2: con atributos
[class*="mi-clase-de-anuncio"] { display: none !important; }

// Variación 3: padre + hijo
.ad-wrapper .mi-clase-de-anuncio { display: none !important; }
```

**Paso 4: Probar en navegador primero**
```javascript
// DevTools → Console
// Agrega CSS temporalmente
var style = document.createElement('style');
style.innerHTML = '.mi-clase-de-anuncio { display: none !important; }';
document.head.appendChild(style);

// Si funciona aquí, el selector es correcto
// Luego lo copias a tu código
```

---

## 8. Caso: Reddit Está Roto (Debugging)

### Problema
Después de agregar un filtro, Reddit no carga correctamente.

### Diagnóstico

**Opción 1: Revert rápido**
```bash
# Revert el último cambio
git checkout app/src/main/java/ia/ankherth/reddit/DOMStyleInjector.kt

# Compilar
./gradlew build
./gradlew installDebug

# Si funciona, el problema era ese archivo
```

**Opción 2: Deshabilitar reglas una por una**
```kotlin
// En BLOCKING_CSS, comenta reglas
/*
.mi-regla-nueva {
    display: none !important;
}
*/

// Compilar y probar
./gradlew build

// Si funciona, esa regla estaba rompiendo todo
```

**Opción 3: Hacer el selector más específico**
```kotlin
// Demasiado general (rompe todo)
div { display: none !important; }

// Más específico
.advertencia-container div { display: none !important; }

// Más específico aún
.advertencia-container > div.item { display: none !important; }
```

---

## 9. Monitorear Qué Se Bloquea

### Crear un reporte de seguridad
```kotlin
// Modificar ContentSanitizer.kt para guardar log detallado
private val blockedElements = mutableListOf<String>()

private fun logRemovedElement(tag: String, value: String) {
    val entry = "$tag: ${value.take(50)}"
    blockedElements.add(entry)
    println("[SECURITY] $entry")
}

fun getBlockedElementsList(): List<String> = blockedElements.toList()
```

### Ver el reporte
```kotlin
// En MainActivity, al terminar una sesión
override fun onPause() {
    super.onPause()
    val list = contentSanitizer.getBlockedElementsList()
    println("[REPORT] Blocked ${list.size} elements this session")
    list.forEach { println(it) }
}
```

---

## 10. Exportar Logs a Servidor

### Para análisis posterior
```kotlin
// ContentSanitizer.kt
private fun logRemovedElement(tag: String, value: String) {
    println("[SECURITY] Removed $tag element: $value")
    
    // Enviar a servidor de logs (opcional)
    try {
        val json = """
        {
            "type": "removed_element",
            "tag": "$tag",
            "value": "${value.take(100)}",
            "timestamp": "${System.currentTimeMillis()}"
        }
        """
        
        // TODO: Enviar json a tu servidor de logs
        // sendToServer("/api/security-logs", json)
    } catch (e: Exception) {
        // Fallar silenciosamente
    }
}
```

---

## 11. Pruebas Manuales Checklist

```
□ Reddit carga sin congelarse
□ Feed principal visible
□ Puedo scrollear sin lag
□ Comentarios se expanden
□ Puedo upvote/downvote
□ Búsqueda funciona
□ Videos se reproducen
□ Imágenes cargan rápido
□ Navegación entre subs funciona
□ Logging muestra eventos esperados
□ No hay errores en Logcat
```

---

## 12. Plantilla para Nuevo Filtro

### Cuando quieras agregar un filtro rápidamente

**Plantilla:**
```kotlin
// 1. Decidir dónde va (HTTP, HTML, o DOM)
// HTTP → ContentInterceptor
// HTML → ContentSanitizer
// DOM → DOMStyleInjector

// 2. Identificar qué bloquear
// Dominio: "tracking.com"
// Clase: "ad-banner"
// Atributo: "data-tracking-id"

// 3. Agregar el filtro
// En el archivo correspondiente, buscar la sección
// Agregar el nuevo item a la lista

// 4. Compilar
./gradlew build

// 5. Probar
./gradlew installDebug
adb logcat | grep "nuevo-filtro"

// 6. Verificar no se rompió nada
// Revisar el checklist anterior
```

---

## Resumen de Archivos a Modificar

| Necesidad | Archivo | Sección |
|-----------|---------|---------|
| Bloquear dominio | `ContentInterceptor.kt` | `BLOCKED_TRACKING_DOMAINS` |
| Ocultar elemento | `DOMStyleInjector.kt` | `BLOCKING_CSS` |
| Filtrar HTML | `ContentSanitizer.kt` | `BLOCKED_TAG_PATTERNS` |
| Bloquear script dinámico | `DOMStyleInjector.kt` | `BLOCKED_SCRIPT_PATTERNS` |
| Permitir iframe | `ContentSanitizer.kt` | `TRUSTED_IFRAME_DOMAINS` |

---

## 🎯 Tips de Experto

1. **Siempre usar `!important` en CSS**
   - Sin esto, estilos pueden ser sobrescritos

2. **Probar selectores en navegador primero**
   - Ahorra tiempo de compilación

3. **Hacer selectores específicos**
   - Evita bloques accidentales de contenido válido

4. **Mantener logs limpios**
   - Ayuda a debugging futuro

5. **Documentar cambios**
   - Explica por qué agregaste cada filtro

6. **Revisar cambios con diff**
   - `git diff` para ver exactamente qué cambió

7. **Compilar frecuentemente**
   - Pequeños cambios = fácil debugging

8. **Probar en múltiples subreddits**
   - Diferentes designs pueden romper de formas diferentes

---

**Última actualización:** 1 de diciembre de 2025
