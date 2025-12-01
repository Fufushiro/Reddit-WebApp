# ⚡ Guía Rápida de Inicio (5 Minutos)

Quieres comenzar YA. Aquí está todo lo que necesitas en 5 minutos.

---

## 🚀 Paso 1: Compilar (2 minutos)

```bash
cd "Reddit WebApp"
./gradlew clean build
```

**Resultado esperado:**
```
BUILD SUCCESSFUL
```

---

## 📦 Paso 2: Instalar (1 minuto)

```bash
./gradlew installDebug
```

**O desde Android Studio:** Run > Run 'app' (Ctrl+R)

---

## ✅ Paso 3: Probar (2 minutos)

1. Abre la app
2. Reddit debe cargar
3. ¡Listo! Tendrás:
   - ✅ Sin rastreo
   - ✅ Sin anuncios
   - ✅ Sin scripts maliciosos

---

## 📊 Ver Logs (Opcional)

```bash
adb logcat | grep SECURITY
```

Verás eventos como:
```
[SECURITY] Removed onclick from <div>
[INTERCEPTOR] Blocked tracking: google-analytics.com
[INJECTOR] CSS security layer injected
```

---

## 🔧 Agregar Nuevo Filtro (Opcional)

### Ejemplo: Bloquear "nuevo-rastreador.com"

**1. Abre:** `ContentInterceptor.kt`

**2. Encuentra:**
```kotlin
private val BLOCKED_TRACKING_DOMAINS = setOf(
    "google-analytics.com",
    // ...
)
```

**3. Agrega:**
```kotlin
"nuevo-rastreador.com"
```

**4. Compila:**
```bash
./gradlew build && ./gradlew installDebug
```

**5. Listo.** El nuevo dominio se bloquea automáticamente.

---

## 📚 Documentación Completa

Si quieres **más detalles**, lee:

| Documento | Para... |
|-----------|---------|
| **README.md** | Visión general |
| **INDEX.md** | Navegar la documentación |
| **IMPLEMENTATION_GUIDE.md** | Entender todo |
| **EXAMPLES.md** | Aprender con ejemplos |
| **TESTING_CHECKLIST.md** | Testing y debugging |

---

## 🐛 Algo No Funciona?

### "Reddit no carga"
```bash
# Ver logs de error
adb logcat | grep "ERROR\|CRASH"

# Revert cambios
git checkout ContentSanitizer.kt
./gradlew build
```

### "Un anuncio sigue apareciendo"
```
Ver EXAMPLES.md - Ejemplo 7
```

### "Videos no reproducen"
```
Revisar ContentSanitizer.kt
Asegurar que youtube.com está en TRUSTED_IFRAME_DOMAINS
```

---

## ✨ Lo que Hace

```
Reddit SIN:           Reddit CON:
❌ Rastreo           ✅ Privacidad
❌ Anuncios          ✅ Contenido limpio
❌ Scripts 3eros     ✅ Seguridad
❌ Data exfil.       ✅ Control
```

---

## 🎯 Comandos Útiles

```bash
# Compilar
./gradlew build

# Compilar + instalar
./gradlew installDebug

# Ver logs
adb logcat | grep SECURITY

# Limpiar
./gradlew clean

# Ver ayuda
./gradlew tasks
```

---

## 📝 Estructura

- **ContentInterceptor.kt** → Bloquea rastreo HTTP
- **ContentSanitizer.kt** → Limpia HTML
- **DOMStyleInjector.kt** → Inyecta protección DOM
- **MainActivity.kt** → Punto de entrada

---

## ⚠️ Importante

Este es para **uso personal solamente**. Revisar:
- https://www.reddit.com/r/reddit.com/wiki/user_agreement

---

## 🎉 ¡Listo!

Ya deberías tener Reddit seguro en tu dispositivo.

**Próximos pasos:**
1. Navega por Reddit normalmente
2. Ver los logs: `adb logcat | grep SECURITY`
3. Agregar filtros personalizados si quieres
4. Revisar documentación completa si tienes dudas

---

## 📚 Para Más Información

- Documentación completa → Abre **INDEX.md**
- Entender el código → Lee **IMPLEMENTATION_GUIDE.md**
- Ejemplos prácticos → Ve **EXAMPLES.md**
- Testing → Consulta **TESTING_CHECKLIST.md**

---

**¡Disfruta Reddit sin rastreo!** 🚀
