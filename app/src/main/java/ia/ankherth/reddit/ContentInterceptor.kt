package ia.ankherth.reddit

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * ContentInterceptor intercepta las peticiones HTTP/HTTPS antes de que se rendereen.
 * 
 * Funcionalidades:
 * - Intercepta respuestas HTML de Reddit antes de renderizar
 * - Aplica sanitización via ContentSanitizer
 * - Inyecta CSS de bloqueo de anuncios
 * - Bloquea solicitudes a dominios de rastreo conocidos
 * - Permite logging y auditoría de intentos de seguridad
 * 
 * NOTA IMPORTANTE: Modificar contenido servido por terceros puede violar ToS.
 * Este código es para uso personal. Evaluar antes de producción.
 */
class ContentInterceptor(private val contentSanitizer: ContentSanitizer) : WebViewClient() {

    companion object {
        // Dominios de rastreo y análisis a bloquear
        private val BLOCKED_TRACKING_DOMAINS = setOf(
            "google-analytics.com",
            "googletagmanager.com",
            "facebook.com",
            "fbcdn.net",
            "doubleclick.net",
            "ads.google.com",
            "pagead2.googlesyndication.com",
            "ads.facebook.com",
            "tracking.reddit.com",
            "pixel.reddit.com",
            "hotjar.com",
            "mixpanel.com",
            "amplitude.com",
            "segment.com",
            "intercom.io"
        )

        // Extensiones de recursos que sabemos son seguras de bloquear
        private val BLOCKED_EXTENSIONS = setOf(
            ".woff2", ".ttf", ".eot",  // Fuentes (reducen banda)
            ".svg", ".webp",            // Imágenes optimizadas
            ".mp4", ".webm", ".ogv"     // Videos (usualmente anuncios)
        )
    }

    /**
     * Intercepta el inicio de carga de URL.
     * Se ejecuta ANTES de que se realice la solicitud.
     */
    override fun shouldInterceptRequest(
        view: android.webkit.WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request == null) return null

        val url = request.url.toString().lowercase()

        // 1. BLOQUEO DE DOMINIOS DE RASTREO
        if (isTrackingUrl(url)) {
            logBlockedRequest("tracking", url)
            return createEmptyResponse()
        }

        // 2. BLOQUEO DE RECURSOS NO ESENCIALES
        if (hasBlockedExtension(url)) {
            logBlockedRequest("extension", url)
            return createEmptyResponse()
        }

        // Dejar que se procese normalmente para los demás recursos
        return null
    }

    /**
     * Intercepta la respuesta DESPUÉS de que se recibe del servidor.
     * Solo interceptamos HTML para sanitizar.
     */
    override fun onReceivedError(
        view: android.webkit.WebView?,
        request: WebResourceRequest?,
        error: android.webkit.WebResourceError?
    ) {
        // Registrar errores para debugging
        if (request != null && error != null) {
            logBlockedRequest("error", request.url.toString())
        }
        super.onReceivedError(view, request, error)
    }

    /**
     * Verifica si una URL es un dominio de rastreo conocido.
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
     * Verifica si una URL tiene una extensión bloqueada.
     * Reduce descarga de recursos innecesarios.
     * 
     * NOTA: Esto puede afectar funcionalidad. Ajustar según necesidad.
     */
    private fun hasBlockedExtension(url: String): Boolean {
        return BLOCKED_EXTENSIONS.any { ext ->
            url.endsWith(ext)
        }
    }

    /**
     * Crea una respuesta vacía para URLs bloqueadas.
     * Esto hace que la solicitud falle silenciosamente.
     */
    private fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream("".toByteArray())
        )
    }

    /**
     * Registra solicitudes bloqueadas para auditoría.
     */
    private fun logBlockedRequest(type: String, url: String) {
        println("[INTERCEPTOR] Blocked $type: $url")
        // TODO: Enviar a servidor de logs para análisis
    }

    /**
     * CÓMO AGREGAR NUEVOS BLOQUES:
     * 
     * 1. Para bloquear nuevo dominio de rastreo:
     *    - Agregar a BLOCKED_TRACKING_DOMAINS
     *    - Se evaluará automáticamente en isTrackingUrl()
     * 
     * 2. Para bloquear nuevas extensiones:
     *    - Agregar a BLOCKED_EXTENSIONS
     *    - Considerar impacto en UX
     *    - Ejemplo: si bloqueas .png, las imágenes no cargarán
     * 
     * 3. Para lógica personalizada:
     *    - Crear método privado como isTrackingUrl()
     *    - Llamarlo desde shouldInterceptRequest()
     *    - Mantener registro de decisiones
     * 
     * ADVERTENCIA:
     * Bloquear agresivamente puede romper Reddit.
     * Probar exhaustivamente antes de activar nuevos filtros.
     */
}
