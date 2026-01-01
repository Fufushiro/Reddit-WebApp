package ia.ankherth.reddit

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * ContentInterceptor implementa bloqueo de solicitudes tipo uBlock Origin.
 * 
 * Funcionalidades:
 * - Intercepta y bloquea dominios de rastreo conocidos
 * - Bloquea solicitudes a servidores de ads
 * - Bloquea analytics y tracking pixels
 * - Optimiza performance bloqueando recursos innecesarios
 * - Mantiene funcionalidad core de Reddit intacta
 * 
 * PRINCIPIOS:
 * 1. Privacidad first: bloquear tracking sin afectar UX
 * 2. Rendimiento: reducir requests innecesarias
 * 3. Seguridad: prevenir inyección de scripts maliciosos
 * 
 * NOTA IMPORTANTE: Modificar contenido servido por terceros puede violar ToS.
 * Este código es para uso personal. Evaluar antes de producción.
 */
class ContentInterceptor(private val contentSanitizer: ContentSanitizer) : WebViewClient() {

    companion object {
        // Dominios de rastreo y análisis a bloquear (estilo uBlock)
        private val BLOCKED_TRACKING_DOMAINS = setOf(
            // Google Analytics & Tag Manager
            "google-analytics.com",
            "googletagmanager.com",
            "analytics.google.com",
            "stats.g.doubleclick.net",
            
            // Facebook & Meta
            "facebook.com",
            "fbcdn.net",
            "connect.facebook.net",
            "facebook.net",
            
            // DoubleClick & Google Ads
            "doubleclick.net",
            "ads.google.com",
            "pagead2.googlesyndication.com",
            "googleadservices.com",
            "googlesyndication.com",
            
            // Reddit specific tracking
            "tracking.reddit.com",
            "pixel.reddit.com",
            "reddit.com/tracking",
            
            // Third-party analytics
            "hotjar.com",
            "hcaptcha.com",  // Bloqueador de bots (pero afecta privacidad)
            "mixpanel.com",
            "amplitude.com",
            "segment.com",
            "intercom.io",
            "appsflyer.com",
            "branch.io",
            
            // Ad networks
            "adnxs.com",
            "advertising.com",
            "casale.com",
            "criteo.com",
            "exponential.com",
            "medianet.com",
            "sovrn.com",
            "taboola.com",
            "outbrain.com",
            
            // Behavioral & marketing
            "moatads.com",
            "imrworldwide.com",
            "doubleverify.com",
            "c3metrics.com",
            
            // CDN de ads
            "ads-serving-web.com",
            "adtech.amazon.com"
        )

        // Patrones de URLs a bloquear (más específico que dominios)
        private val BLOCKED_URL_PATTERNS = listOf(
            Regex("/ad[s]?/", RegexOption.IGNORE_CASE),
            Regex("/analytics", RegexOption.IGNORE_CASE),
            Regex("/tracking", RegexOption.IGNORE_CASE),
            Regex("/pixel", RegexOption.IGNORE_CASE),
            Regex("\\?utm_", RegexOption.IGNORE_CASE),
            Regex("\\?gclid=", RegexOption.IGNORE_CASE),
            Regex("/beacon", RegexOption.IGNORE_CASE),
            Regex("/log\\?", RegexOption.IGNORE_CASE),
            Regex("/metrics", RegexOption.IGNORE_CASE)
        )

        // Extensiones de recursos que sabemos son seguras de bloquear
        private val BLOCKED_EXTENSIONS = setOf(
            ".woff2", ".ttf", ".eot", ".otf"  // Fuentes (reducen banda, no afecta core)
            // NOTA: Removed .svg, .webp, .mp4 ya que pueden ser contenido legítimo de Reddit
        )
        
        // User-Agent strings a bloquear (rastreadores específicos)
        private val BLOCKED_USER_AGENTS = listOf(
            "googlebot",
            "bingbot",
            "crawler",
            "spider",
            "scraper"
        )
    }

    /**
     * Intercepta el inicio de carga de URL.
     * Se ejecuta ANTES de que se realice la solicitud (oportunidad de bloqueo).
     */
    override fun shouldInterceptRequest(
        view: android.webkit.WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request == null) return null

        val url = request.url.toString().lowercase()

        // 1. BLOQUEO DE DOMINIOS DE RASTREO (uBlock-style)
        if (isTrackingUrl(url)) {
            logBlockedRequest("tracking_domain", request.url.toString())
            return createEmptyResponse()
        }

        // 2. BLOQUEO DE PATRONES DE RASTREO EN URLs
        if (matchesBlockedPattern(url)) {
            logBlockedRequest("tracking_pattern", request.url.toString())
            return createEmptyResponse()
        }

        // 3. BLOQUEO DE RECURSOS NO ESENCIALES (optimización)
        if (hasBlockedExtension(url)) {
            logBlockedRequest("extension", request.url.toString())
            return createEmptyResponse()
        }

        // Dejar que se procese normalmente para los demás recursos
        return null
    }

    /**
     * Intercepta errores de carga para debugging.
     */
    override fun onReceivedError(
        view: android.webkit.WebView?,
        request: WebResourceRequest?,
        error: android.webkit.WebResourceError?
    ) {
        if (request != null && error != null) {
            logBlockedRequest("load_error", request.url.toString())
        }
        super.onReceivedError(view, request, error)
    }

    /**
     * Verifica si una URL es un dominio de rastreo conocido (método principal).
     * 
     * @param url URL a verificar (en lowercase)
     * @return true si detecta rastreo
     */
    private fun isTrackingUrl(url: String): Boolean {
        return BLOCKED_TRACKING_DOMAINS.any { domain ->
            url.contains(domain)
        }
    }

    /**
     * Verifica si una URL cumple con patrones de rastreo.
     * Más específico que búsqueda de dominios.
     * 
     * Ejemplos bloqueados:
     * - https://example.com/ads/banner.jpg
     * - https://example.com/tracking?id=123
     * - https://example.com/pixel.gif?utm_source=reddit
     * 
     * @param url URL a verificar (en lowercase)
     * @return true si cumple patrón de rastreo
     */
    private fun matchesBlockedPattern(url: String): Boolean {
        return BLOCKED_URL_PATTERNS.any { pattern ->
            pattern.containsMatchIn(url)
        }
    }

    /**
     * Verifica si una URL tiene una extensión bloqueada.
     * Reduce descarga de recursos innecesarios sin afectar contenido principal.
     * 
     * NOTA: Esto afecta bandwidth pero no UX de Reddit (posts siguen viéndose).
     * Las fuentes se descargan desde servidores de Google en algunos casos.
     */
    private fun hasBlockedExtension(url: String): Boolean {
        return BLOCKED_EXTENSIONS.any { ext ->
            url.endsWith(ext)
        }
    }

    /**
     * Crea una respuesta vacía para URLs bloqueadas.
     * Esto hace que la solicitud falle silenciosamente sin afectar parsing del HTML.
     * 
     * @return Respuesta vacía que el navegador interpreta como "recurso no disponible"
     */
    private fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream("".toByteArray())
        )
    }

    /**
     * Registra solicitudes bloqueadas para auditoría y debugging.
     * En logs podemos ver qué intentó cargar Reddit y fue bloqueado.
     */
    private fun logBlockedRequest(type: String, url: String) {
        println("[INTERCEPTOR] [$type] $url")
        // TODO: Enviar a servidor de logs seguro para análisis agregado
    }

    /**
     * ============================================
     * CÓMO AGREGAR NUEVOS BLOQUES (GUÍA)
     * ============================================
     * 
     * 1. PARA BLOQUEAR NUEVO DOMINIO DE RASTREO:
     *    - Agregar a BLOCKED_TRACKING_DOMAINS
     *    - Ejemplo: "newtracker.com"
     *    - Se evaluará automáticamente en isTrackingUrl()
     *    - Impacto: se bloquean TODAS las requests a ese dominio
     * 
     * 2. PARA BLOQUEAR PATRONES DE URL:
     *    - Agregar Regex a BLOCKED_URL_PATTERNS
     *    - Ejemplo: Regex("/ads/", IGNORE_CASE) para bloquear /ads/ en cualquier URL
     *    - Se evaluará en matchesBlockedPattern()
     *    - Más flexible que dominios pero requiere pruebas
     * 
     * 3. PARA BLOQUEAR EXTENSIONES:
     *    - Agregar a BLOCKED_EXTENSIONS
     *    - ADVERTENCIA: Solo agregar extensiones que NO sean contenido principal
     *    - ✅ OK: .woff2, .ttf (fuentes)
     *    - ❌ NO: .jpg, .png, .mp4 (pueden ser posts de Reddit)
     * 
     * 4. TESTING DESPUÉS DE CAMBIOS:
     *    - Cargar Reddit y revisar logs en Android Studio Logcat
     *    - Verificar que posts se cargan correctamente
     *    - Verificar que comentarios aparecen
     *    - Verificar que se puede interactuar (upvote, expand, etc.)
     *    - Si algo falla: comentar la regla nueva y reintentar más específico
     * 
     * ============================================
     * REGLAS DE ORO
     * ============================================
     * ✅ SÍ: Bloquear dominios terceros que NO son reddit.com
     * ✅ SÍ: Bloquear patrones que claramente son tracking (/tracking/, /pixel/, etc.)
     * ✅ SÍ: Bloquear extensiones que no son contenido (fuentes, beacons)
     * 
     * ❌ NO: Bloquear recursos de reddit.com (rompe la app)
     * ❌ NO: Bloquear extensiones de medios (.jpg, .png, .mp4)
     * ❌ NO: Bloquear todo indiscriminadamente
     */
}

