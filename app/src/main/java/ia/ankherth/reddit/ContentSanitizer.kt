package ia.ankherth.reddit

/**
 * ContentSanitizer es el componente principal que sanitiza HTML/DOM.
 * 
 * Funcionalidades:
 * - Elimina scripts externos e inyectados
 * - Filtra iframes desconocidos (solo permite iframes de fuentes confiables)
 * - Elimina eventos inline maliciosos (onclick, onload, etc.)
 * - Filtra atributos data y archivos adjuntos sospechosos
 * - Preserva contenido legítimo de Reddit
 * 
 * NOTA IMPORTANTE: Este código sanitiza contenido antes de renderizarlo.
 * Verifica los términos de servicio de Reddit sobre modificación de contenido.
 */
class ContentSanitizer {

    companion object {
        // Dominios confiables desde los que permitimos iframes
        // (Agregar solo después de verificación de seguridad)
        private val TRUSTED_IFRAME_DOMAINS = setOf(
            "reddit.com",
            "redditmedia.com",
            "redditcdn.com",
            "i.redd.it",
            "v.redd.it",
            "imgur.com",
            "youtube.com",
            "youtu.be"
        )

        // Patrones de etiquetas completamente prohibidas por seguridad
        private val BLOCKED_TAG_PATTERNS = listOf(
            Regex("<script[^>]*>.*?</script>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)),  // Scripts
            Regex("<iframe[^>]*(?!sandbox)[^>]*>", setOf(RegexOption.IGNORE_CASE)),  // iframes no seguros
            Regex("<embed[^>]*>", setOf(RegexOption.IGNORE_CASE)),  // Embeds
            Regex("<object[^>]*>.*?</object>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)),  // Objects
            Regex("<link[^>]*(?!favicon)[^>]*>", setOf(RegexOption.IGNORE_CASE)),  // Links que no sean favicon
            Regex("<meta\\s+name=['\"](?:tracking|google-site-verification)['\"][^>]*>", setOf(RegexOption.IGNORE_CASE)),  // Meta de rastreo
            Regex("<noscript[^>]*>.*?</noscript>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))  // Noscript
        )

        // Patrones de atributos peligrosos
        private val DANGEROUS_ATTRIBUTE_PATTERNS = listOf(
            Regex("\\s+on\\w+\\s*=", RegexOption.IGNORE_CASE),  // onclick, onload, etc.
            Regex("\\s+data-\\S*\\s*=", RegexOption.IGNORE_CASE),  // data-*
            Regex("\\s+jsaction\\s*=", RegexOption.IGNORE_CASE),  // jsaction
            Regex("\\s+ng-\\S*\\s*=", RegexOption.IGNORE_CASE)  // ng-*
        )
    }

    /**
     * Sanitiza HTML eliminando contenido malicioso.
     * Usa expresiones regulares para máxima compatibilidad en Android.
     * 
     * @param htmlContent HTML sin sanitizar de Reddit o API
     * @return HTML sanitizado y seguro para renderizar
     */
    fun sanitizeHtml(htmlContent: String): String {
        return try {
            var sanitized = htmlContent
            
            // 1. Eliminar scripts bloqueados
            sanitized = removeBlockedTags(sanitized)
            
            // 2. Limpiar atributos peligrosos
            sanitized = cleanDangerousAttributes(sanitized)
            
            // 3. Filtrar iframes no confiables
            sanitized = filterIframes(sanitized)
            
            // 4. Agregar headers de seguridad
            sanitized = addSecurityHeaders(sanitized)
            
            sanitized
        } catch (e: Exception) {
            println("Error sanitizing HTML: ${e.message}")
            "<div>Contenido no disponible</div>"
        }
    }

    /**
     * Elimina etiquetas bloqueadas usando regex.
     */
    private fun removeBlockedTags(html: String): String {
        var result = html
        BLOCKED_TAG_PATTERNS.forEach { pattern ->
            val matches = pattern.findAll(result)
            matches.forEach { match ->
                logRemovedElement(pattern.pattern, match.value)
            }
            result = result.replace(pattern, "")
        }
        return result
    }

    /**
     * Limpia atributos peligrosos del HTML.
     */
    private fun cleanDangerousAttributes(html: String): String {
        var result = html
        DANGEROUS_ATTRIBUTE_PATTERNS.forEach { pattern ->
            result = result.replace(pattern, " ")
        }
        return result
    }

    /**
     * Filtra iframes no confiables.
     */
    private fun filterIframes(html: String): String {
        val iframePattern = Regex("<iframe[^>]*src=['\"]([^'\"]+)['\"][^>]*>.*?</iframe>", 
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        
        return html.replace(iframePattern) { match ->
            val src = match.groupValues.getOrNull(1) ?: ""
            if (isUrlFromTrustedDomain(src)) {
                // Mantener iframe pero agregar sandbox
                match.value.replace(">", " sandbox=\"allow-same-origin allow-scripts allow-popups\">")
            } else {
                // Remover iframe
                logRemovedElement("iframe", src)
                ""
            }
        }
    }

    /**
     * Agrega headers de seguridad de contenido via meta tags.
     */
    private fun addSecurityHeaders(html: String): String {
        if (html.contains("Content-Security-Policy")) {
            return html
        }
        
        val cspMeta = """<meta http-equiv="Content-Security-Policy" content="script-src 'self'; object-src 'none'; base-uri 'self';">"""
        val headPattern = Regex("</head>", RegexOption.IGNORE_CASE)
        
        return html.replace(headPattern, "$cspMeta</head>")
    }

    /**
     * Verifica si una URL pertenece a un dominio confiable.
     */
    private fun isUrlFromTrustedDomain(url: String): Boolean {
        if (url.isEmpty() || url.startsWith("javascript:")) return false
        
        return TRUSTED_IFRAME_DOMAINS.any { domain ->
            url.contains(domain)
        }
    }

    /**
     * Registra elementos removidos para auditoría.
     */
    private fun logRemovedElement(tag: String, value: String) {
        println("[SECURITY] Removed element: $tag - Value: ${value.take(50)}")
    }

    /**
     * CÓMO AGREGAR NUEVOS FILTROS:
     * 
     * 1. Para nuevas etiquetas prohibidas:
     *    - Agregar a BLOCKED_TAG_PATTERNS como Regex
     *    - La lógica en removeBlockedTags() las elimina automáticamente
     * 
     * 2. Para nuevos dominios confiables (iframes):
     *    - Agregar a TRUSTED_IFRAME_DOMAINS
     *    - Requiere análisis de seguridad previo
     * 
     * 3. Para nuevos atributos peligrosos:
     *    - Agregar a DANGEROUS_ATTRIBUTE_PATTERNS como Regex
     *    - cleanDangerousAttributes() los eliminará
     * 
     * Ejemplo:
     * // Bloquear toda etiqueta de video
     * Regex("<video[^>]*>.*?</video>", RegexOption.DOT_MATCHES_ALL or RegexOption.IGNORE_CASE)
     */
}
