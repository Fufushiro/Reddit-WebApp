/**
 * POLÍTICA DE SEGURIDAD Y FILTRADO - Reddit WebApp
 * ================================================
 * 
 * Este documento explica qué se filtra, por qué, y cómo se implementa.
 * 
 * NOTA IMPORTANTE SOBRE TÉRMINOS DE SERVICIO:
 * ==========================================
 * 
 * 1. MODIFICACIÓN DE CONTENIDO
 *    - Esta aplicación MODIFICA el contenido entregado por Reddit
 *    - Específicamente: oculta anuncios, remueve scripts de rastreo
 *    - Esto PODRÍA violar los Términos de Servicio de Reddit
 *    - Se recomienda revisar: https://www.reddit.com/r/reddit.com/wiki/user_agreement
 *    - USO PERSONAL SOLAMENTE - No está aprobado para distribución comercial
 * 
 * 2. RASTREO Y PRIVACIDAD
 *    - Reddit usa rastreo para monetización (publicidad)
 *    - Al bloquearlo, reduces ingresos de Reddit
 *    - Esto es compensación por usar su servicio sin ver anuncios
 *    - Considera: ¿Usarías adblocker en el navegador? Esto es equivalente
 * 
 * 3. SCRIPTS DE TERCEROS
 *    - Reddit carga scripts de Google Analytics, Facebook, etc.
 *    - Estos rastrean tu comportamiento entre sitios
 *    - Bloquearlos mejora tu privacidad pero afecta a Reddit's analytics
 * 
 * ================================================
 * 
 * COMPONENTES QUE SE FILTRAN Y POR QUÉ:
 * ====================================
 * 
 * SCRIPTS BLOQUEADOS:
 * - Google Analytics (googletagmanager.com): Rastreo de comportamiento
 * - Google Ads (doubleclick.net, pagead2): Publicidad y rastreo
 * - Facebook Pixel (facebook.com, fbcdn.net): Tracking entre sitios
 * - Otros análisis: Hotjar, Mixpanel, Amplitude, Segment
 * 
 * Impacto: Los datos de comportamiento no se envían a terceros
 * 
 * ANUNCIOS OCULTOS (VIA CSS):
 * - Contenido marcado como "sponsored"
 * - Posts promocionales
 * - Banners publicitarios
 * - Secciones "Promoted"
 * 
 * Impacto: Menos ingresos para Reddit, mejor UX sin distracciones
 * 
 * IFRAMES BLOQUEADOS:
 * - Iframes no identificados (excepto de dominios confiables)
 * - Iframes que cargan desde dominios de rastreo
 * 
 * Impacto: Algunos contenido embebido puede no cargar
 * 
 * ATRIBUTOS PELIGROSOS REMOVIDOS:
 * - data-* (rastreo de eventos y conversiones)
 * - onclick/onerror (inyección de código)
 * - jsaction (Google event handling)
 * - ng-* (Angular directives - potencial XSS)
 * 
 * Impacto: Funcionalidad limitada en algunos elementos interactivos
 * 
 * ================================================
 * 
 * FLUJO TÉCNICO DE SEGURIDAD:
 * ==========================
 * 
 * 1. NIVEL HTTP (Interceptor)
 *    WebView solicita URL
 *           ↓
 *    ContentInterceptor.shouldInterceptRequest()
 *           ↓
 *    ¿Es dominio de rastreo? → SÍ → Bloquear (retornar respuesta vacía)
 *                              → NO → Permitir descarga normal
 * 
 * 2. NIVEL HTML (Sanitizer)
 *    HTML se descarga desde Reddit
 *           ↓
 *    Se parsea con Jsoup
 *           ↓
 *    ContentSanitizer.sanitizeHtml():
 *       - Elimina todas las etiquetas <script>
 *       - Elimina <iframe>, <embed>, <object>
 *       - Elimina todos los atributos on*
 *       - Filtra data-*, ng-*, jsaction
 *           ↓
 *    HTML sanitizado se renderiza en WebView
 * 
 * 3. NIVEL DOM (Client-side JS)
 *    Página cargada
 *           ↓
 *    onPageFinished() → injectSecurityLayers()
 *           ↓
 *    Se inyecta CSS de DOMStyleInjector
 *       - Oculta elementos con data-testid="ad"
 *       - Oculta iframes de rastreo
 *       - Oculta imágenes beacon
 *           ↓
 *    Se inyecta JavaScript de DOMStyleInjector
 *       - MutationObserver vigila nuevos elementos
 *       - Intercepta appendChild/insertBefore para bloquear scripts dinámicos
 *       - Limpia atributos peligrosos en nuevos elementos
 * 
 * ================================================
 * 
 * CÓMO EXTENDER LOS FILTROS:
 * =========================
 * 
 * CASO 1: Quiero bloquear un nuevo dominio de rastreo
 * ────────────────────────────────────────────────
 * Archivo: ContentInterceptor.kt
 * Paso 1: Agregar a BLOCKED_TRACKING_DOMAINS
 * Paso 2: También agregar a DOMStyleInjector.BLOCKING_JAVASCRIPT
 * Paso 3: Probar que Reddit sigue funcionando
 * 
 * Ej:
 *   BLOCKED_TRACKING_DOMAINS = setOf(
 *       ...
 *       "new-tracking-site.com"
 *   )
 * 
 * CASO 2: Quiero ocultar un nuevo tipo de anuncio
 * ──────────────────────────────────────────────
 * Archivo: DOMStyleInjector.kt
 * Paso 1: Identificar el selector CSS del anuncio
 *         (usa DevTools en navegador de escritorio)
 * Paso 2: Agregar a la sección "BLOQUEADORES" en BLOCKING_CSS
 * Paso 3: Usar { display: none !important; }
 * 
 * Ej:
 *   /* Nuevo tipo de anuncio */
 *   .my-custom-ad-class,
 *   [data-custom-ad-attr] {
 *       display: none !important;
 *   }
 * 
 * CASO 3: Quiero evitar que se ejecute un script específico
 * ────────────────────────────────────────────────────────
 * Archivo: DOMStyleInjector.kt, sección BLOCKING_JAVASCRIPT
 * Paso 1: Agregar patrón a BLOCKED_SCRIPT_PATTERNS
 * Paso 2: Probar en navegador que el patrón funciona
 * 
 * Ej:
 *   const BLOCKED_SCRIPT_PATTERNS = [
 *       ...
 *       'mi-nuevo-rastreador'
 *   ];
 * 
 * ================================================
 * 
 * TESTING Y VALIDACIÓN:
 * ====================
 * 
 * Después de cada cambio, verificar:
 * 
 * ✓ Reddit carga sin errores
 * ✓ Feed principal es visible
 * ✓ Puedo ver posts completos
 * ✓ Puedo expandir comentarios
 * ✓ Puedo hacer scroll
 * ✓ Botones de upvote/downvote funcionan
 * ✓ Puedo navegar entre subreddits
 * ✓ Búsqueda funciona
 * 
 * Si algo no funciona:
 * 1. Revisar la consola (activar Developer Mode si está disponible)
 * 2. Comentar la regla nueva y probar
 * 3. Hacer el filtro más específico
 * 4. Considerar si realmente es necesario
 * 
 * ================================================
 * 
 * SOLUCIÓN DE PROBLEMAS:
 * ====================
 * 
 * "Reddit no carga completamente"
 * → Probablemente un filtro de CSS bloqueó contenido esencial
 * → Revisar DOMStyleInjector.BLOCKING_CSS
 * → Usar selectores más específicos
 * 
 * "Los videos no juegan"
 * → Podrían estar en iframes bloqueados
 * → Agregar dominio a TRUSTED_IFRAME_DOMAINS en ContentSanitizer
 * 
 * "Aparecen errores de CORS"
 * → Normal cuando se bloquean solicitudes
 * → Estos errores son silenciosos en la mayoría de casos
 * 
 * "Sigue apareciendo un anuncio"
 * → El selector CSS no está lo suficientemente específico
 * → Usar DevTools en navegador de escritorio para identificar clase exacta
 * → Probar el selector en DevTools primero
 * 
 * ================================================
 * 
 * CONSIDERACIONES DE PRIVACIDAD:
 * =============================
 * 
 * ✓ LO QUE ESTA APP PROTEGE:
 * - Tu actividad NO se envía a Google Analytics
 * - Tu actividad NO se envía a Facebook
 * - Tus clics en anuncios NO se rastrean
 * - Tus datos NO se venden a brokers
 * 
 * ✗ LO QUE REDDIT AÚN PUEDE VER:
 * - Tu IP (Reddit sabe dónde estás)
 * - Qué subreddits visitas (logs de servidor)
 * - Qué posts votes/comentas (si estás logged in)
 * - Tu agente de usuario (versión de app)
 * 
 * → Si quieres privacidad máxima, usa VPN + cuenta anónima
 * 
 * ================================================
 * 
 * IMPACTO EN REDDIT COMO NEGOCIO:
 * ==============================
 * 
 * Esta app reduce:
 * - Ingresos por publicidad (anuncios no vistos = no vendidos)
 * - Datos de análisis (sin Google Analytics)
 * - Perfilado de usuarios (sin Facebook Pixel)
 * 
 * Esto es similar a:
 * - Usar adblocker en navegador
 * - Desactivar cookies en navegador
 * - Usar modo incógnito
 * 
 * Reddit tiene derecho a:
 * - Cerrar tu cuenta
 * - Bloquear el acceso
 * - Cambiar ToS
 * 
 * Este código es para USO PERSONAL. No intentes distribuirlo.
 * 
 * ================================================
 * 
 * AUDITORÍA Y LOGS:
 * ================
 * 
 * Todas las acciones de seguridad se loguean:
 * 
 * [SECURITY] Removed <tag> element: <details>
 * [SECURITY] Removed <attribute> from <tag>
 * [INTERCEPTOR] Blocked <type>: <url>
 * [INJECTOR] CSS security layer injected
 * [INJECTOR] JavaScript security layer injected
 * [SECURITY EVENT] <type>: <details>
 * 
 * Puedes ver estos logs en:
 * - Logcat de Android Studio (si ejecutas en emulador/USB debug)
 * - Consola de Developer Tools (si está disponible)
 * 
 * Esto te permite verificar que los filtros funcionan.
 * 
 * ================================================
 * 
 * ARCHIVO DE CONFIGURACIÓN:
 * Este archivo: SecurityPolicy.kt (comentario)
 * 
 * ARCHIVOS DE IMPLEMENTACIÓN:
 * - ContentSanitizer.kt: Elimina elementos HTML peligrosos
 * - ContentInterceptor.kt: Bloquea solicitudes de rastreo
 * - DOMStyleInjector.kt: Inyecta CSS y JavaScript
 * - MainActivity.kt: Punto de entrada e integración
 * 
 * ================================================
 */

// Este archivo es solo documentación. No contiene código ejecutable.
