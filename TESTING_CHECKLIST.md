# ✅ Checklist de Instalación, Compilación y Testing

## 📋 Pre-compilación

- [ ] Clonar repositorio
- [ ] Abrir en Android Studio
- [ ] JDK 11+ instalado (`java -version`)
- [ ] Gradle 8.0+ (`./gradlew --version`)
- [ ] Android SDK instalado
- [ ] Emulador o dispositivo conectado

## 🔨 Compilación

```bash
# Navegar al directorio
cd "Reddit WebApp"

# Limpiar build anterior
./gradlew clean

# Compilar proyecto
./gradlew build

# Si hay error, compilar solo Kotlin
./gradlew compileDebugKotlin
```

### Resultado Esperado
```
BUILD SUCCESSFUL in X seconds
```

- [ ] Compilación sin errores
- [ ] Compilación sin warnings críticos
- [ ] Tamaño de APK < 10MB

## 📦 Instalación

```bash
# Instalar en emulador/dispositivo
./gradlew installDebug

# O desde Android Studio:
# Run > Run 'app' (Ctrl+R)
```

### Resultado Esperado
```
Successfully installed app on device
```

- [ ] Instalación exitosa
- [ ] App aparece en launcher
- [ ] App puede abrir

## 🧪 Testing Funcional

### Test 1: Carga Inicial
- [ ] Abrir app
- [ ] Reddit carga correctamente
- [ ] Feed principal visible
- [ ] Sin errores de crash

### Test 2: Navegación Básica
- [ ] Puedo hacer scroll
- [ ] Puedo abrir posts
- [ ] Puedo cerrar posts
- [ ] Botón atrás funciona
- [ ] Puedo cambiar de subreddit

### Test 3: Interactividad
- [ ] Puedo hacer clic en upvote/downvote
- [ ] Puedo expandir comentarios
- [ ] Puedo escribir comentarios (si estoy logged in)
- [ ] Búsqueda funciona
- [ ] Filtros de Reddit funcionan

### Test 4: Multimedia
- [ ] Imágenes cargan y se muestran
- [ ] Videos se reproducen (si es embed)
- [ ] GIFs funcionan
- [ ] Links funcionan

### Test 5: Rendimiento
- [ ] No hay freezes al scroll
- [ ] Carga es rápida (< 5 segundos)
- [ ] Memoria no se va al infinito
- [ ] CPU usage es bajo en idle

## 🔒 Testing de Seguridad

### Test 1: Rastreo Bloqueado
```bash
adb logcat | grep "INTERCEPTOR"
```
Debe ver logs como:
```
[INTERCEPTOR] Blocked tracking: google-analytics.com/...
[INTERCEPTOR] Blocked tracking: facebook.com/...
```

- [ ] Google Analytics bloqueado
- [ ] Facebook Pixel bloqueado
- [ ] DoubleClick bloqueado

### Test 2: Scripts Removidos
```bash
adb logcat | grep "SECURITY"
```
Debe ver logs como:
```
[SECURITY] Removed <script> element: ...
[SECURITY] Removed onclick from <div>
```

- [ ] Scripts de rastreo eliminados
- [ ] Atributos onclick removidos
- [ ] Data-* removidos

### Test 3: Anuncios Ocultos
- [ ] Los anuncios visibles en navegador NO aparecen en la app
- [ ] Contenido legítimo SI aparece
- [ ] Posts normales visibles
- [ ] Comentarios visibles

### Test 4: CSS Inyectado
```bash
adb logcat | grep "INJECTOR"
```
Debe ver:
```
[INJECTOR] CSS security layer injected
[INJECTOR] JavaScript security layer injected
```

- [ ] CSS se inyecta correctamente
- [ ] JavaScript se inyecta correctamente
- [ ] Sin errores de ejecución

## 🐛 Debugging

### Si algo no funciona

**Opción 1: Ver logs completos**
```bash
adb logcat > /tmp/reddit_logs.txt
# Usar la app por 30 segundos
# Analizar logs
```

**Opción 2: Buscar errores específicos**
```bash
adb logcat | grep -E "ERROR|CRASH|Exception"
```

**Opción 3: Deshabilitar filtros**
Comentar en DOMStyleInjector.kt:
```kotlin
/*
view.evaluateJavascript(
    """javascript:(function() { ... })()""",
    null
)
*/
```
Si funciona sin filtros, el problema es un filtro.

## 📊 Verificación de Rendimiento

### Emulador
```bash
# Abrir app
adb logcat | grep -E "Memory|FPS|CPU"

# O usar Android Studio Profiler:
# View > Tool Windows > Profiler
```

Verificar:
- [ ] Memoria < 200MB
- [ ] CPU < 50% en idle
- [ ] Smooth scrolling (60 FPS)

### Dispositivo Real
- [ ] No se sobrecalienta
- [ ] Batería se consume normalmente
- [ ] App no se congela

## 🎯 Testing en Múltiples Subreddits

Probar en diferentes subreddits para detectar incompatibilidades:

- [ ] r/programming (texto + código)
- [ ] r/pics (imágenes)
- [ ] r/videos (videos)
- [ ] r/worldnews (noticias)
- [ ] r/AskReddit (preguntas)

## 🔄 Regresión Testing

Después de agregar nuevos filtros:

```
1. Compilar
2. Instalar
3. Ejecutar todos los tests básicos (sección "Testing Funcional")
4. Ejecutar todos los tests de seguridad (sección "Testing de Seguridad")
5. Verificar rendimiento (sección "Verificación de Rendimiento")
6. Probar en múltiples subreddits
```

Si todo pasa ✅, el cambio es seguro.

## 📱 Testing en Diferentes Dispositivos

Idealmente probar en:

- [ ] Emulador API 30 (Android 11)
- [ ] Emulador API 31 (Android 12)
- [ ] Emulador API 32 (Android 13)
- [ ] Dispositivo real si es posible

## 🚨 Problemas Comunes y Soluciones

### "BUILD FAILED: Unresolved reference"
```bash
./gradlew --refresh-dependencies clean build
```

### "App crashes on start"
```bash
adb logcat | grep "FATAL\|Exception"
# Ver el stack trace completo
```

### "Reddit no carga"
1. Verificar internet: `adb shell ping google.com`
2. Ver si es problema de filtro
3. Comentar DOMStyleInjector

### "Lento o congela"
1. Ver Profiler en Android Studio
2. Buscar memory leaks
3. Reducir número de logs

### "Videos no juegan"
```kotlin
// En ContentSanitizer.kt, verificar:
TRUSTED_IFRAME_DOMAINS = setOf(
    "youtube.com",  // ← Debe estar
    "youtu.be"
)
```

## 📈 Métricas a Monitorear

Al iniciar la app:

1. **Tiempo de carga**
   - Primero: < 10 segundos
   - Subsecuentes: < 3 segundos

2. **Uso de memoria**
   - Inicial: 100-150MB
   - Después de scroll: < 200MB
   - Máximo: < 300MB

3. **CPU**
   - Idle: < 5%
   - Scroll: 20-50%
   - Máximo: < 80%

4. **Eventos de seguridad**
   - Por página: 10-50 eventos de filtrado
   - Normal: 5-10 scripts bloqueados

## ✅ Aceptación Final

La aplicación está lista si:

- ✅ Compila sin errores
- ✅ Se instala correctamente
- ✅ Reddit carga y funciona
- ✅ Seguridad se activa (logs visibles)
- ✅ Rendimiento aceptable
- ✅ Sin crashes
- ✅ Funciona en múltiples subreddits

## 📝 Reporte de Testing

Crear un archivo `TEST_REPORT.md`:

```markdown
# Reporte de Testing - [Fecha]

## Entorno
- Device: [Emulador/Real]
- Android: [Versión]
- App Version: 1.0

## Resultados
- [ ] Compilación: ✅ PASS
- [ ] Instalación: ✅ PASS
- [ ] Funcionalidad: ✅ PASS
- [ ] Seguridad: ✅ PASS
- [ ] Rendimiento: ✅ PASS

## Problemas Encontrados
(Listar si hay)

## Recomendaciones
(Listar cambios necesarios)
```

---

**Última actualización:** 1 de diciembre de 2025
