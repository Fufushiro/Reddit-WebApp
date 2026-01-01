package ia.ankherth.reddit

/**
 * DOMStyleInjector inyecta CSS y JavaScript en el DOM después de renderizar.
 * 
 * Funcionalidades:
 * - Oculta elementos promocionales y anuncios usando CSS
 * - Inyecta observer de mutaciones para vigilar nuevos elementos
 * - Bloquea scripts que intenten cargarse dinámicamente
 * - Registra intentos de modificación del DOM para auditoría
 * 
 * NOTA: Este archivo contiene CSS y JavaScript que se inyectan via JavaScript Bridge.
 * Se ejecuta DESPUÉS de que el contenido se renderiza en el WebView.
 */
object DOMStyleInjector {

    /**
     * CSS que se inyecta para ocultar anuncios y contenido promocional.
     * 
     * PRINCIPIOS:
     * - Usa !important para asegurar que no se sobrescriba
     * - Oculta sin eliminar del DOM (preserva estructura)
     * - Específico para Reddit pero aplicable a patrones generales
     * - Comentado para facilitar mantenimiento
     */
    val BLOCKING_CSS = """
        <style>
        /* ============================================
           ESPACIO PARA BARRA DE ESTADO / SAFE AREA
           ============================================ */

        html, body {
            /* Fallback genérico para dejar espacio bajo la barra de estado */
            padding-top: 24px !important;

            /* Navegadores con soporte de safe-area (no afecta si no existe) */
            padding-top: constant(safe-area-inset-top) !important;
            padding-top: env(safe-area-inset-top) !important;

            box-sizing: border-box !important;
        }

        /* ============================================
           BLOQUEADORES DE ANUNCIOS Y PROMOCIONALES
           ============================================ */

        /* Ocultar secciones marcadas como sponsored o ads */
        [data-testid*="ad"],
        [data-testid*="sponsored"],
        [class*="promoted"],
        [class*="ad-"],
        [class*="advertisement"],
        [id*="promoted"],
        [id*="advertisement"],
        div[class*="native-ad"],
        article[data-promoted="true"],
        div[data-ad-test-id],
        .promotedlink,
        .AdContainer,
        .ads-container,
        .ad-banner,
        .ad-unit { 
            display: none !important; 
        }

        /* Ocultar enlaces (links) marcados como promoted/sponsored en el feed */
        a[data-click-id*="sponsored"],
        a[data-click-id*="promoted"],
        a[aria-label*="promoted"],
        a[aria-label*="sponsored"],
        a[href*="utm_source=promo"],
        a[href*="utm_medium=sponsored"],
        a[href*="advertiser"],
        a[href*="promoted"] {
            display: none !important;
        }

        /* Ocultar divs que contienen la palabra 'ad' o 'sponsor' en atributos */
        div[data-ad-slot],
        div[data-ad-format],
        aside[data-ad-region],
        section[data-sponsor-content] {
            display: none !important;
        }

        /* Ocultar iframes que comúnmente sirven anuncios */
        iframe[src*="doubleclick"],
        iframe[src*="google"],
        iframe[src*="ads"],
        iframe[src*="pagead"],
        iframe[src*="facebook"],
        iframe[src*="tracking"] {
            display: none !important;
        }

        /* Ocultar contenedores de rastreo (pixel gifs, beacons) */
        img[src*="doubleclick"],
        img[src*="pixel"],
        img[src*="tracking"],
        img[src*="analytics"],
        img[src*="beacon"],
        img[width="1"][height="1"] {
            display: none !important;
            width: 0 !important;
            height: 0 !important;
        }

        /* Ocultar scripts de rastreo si aparecen (redundancia) */
        script[src*="google-analytics"],
        script[src*="googletagmanager"],
        script[src*="facebook"],
        script[src*="tracking"],
        script[src*="hotjar"],
        script[src*="segment"] {
            display: none !important;
        }

        /* Prevenir que scripts inline se ejecuten visiblemente */
        noscript {
            display: none !important;
        }

        /* ============================================
           ESTILOS COMPLEMENTARIOS PARA MEJOR UX
           ============================================ */

        /* Reducir espacios dejados por elementos ocultos */
        main > * {
            margin-bottom: 0 !important;
        }

        /* Asegurar que el contenido principal sea visible */
        article,
        .Post,
        [role="article"],
        div[data-testid*="post"] {
            display: block !important;
            visibility: visible !important;
            opacity: 1 !important;
        }

        /* Proteger comentarios de ser ocultos accidentalmente */
        [data-testid*="comment"],
        .Comment,
        .Comment__body {
            display: block !important;
        }

        /* ============================================
           PREVENCIÓN DE TÉCNICAS ALTERNATIVAS DE RASTREO
           ============================================ */

        /* Bloquear load de imágenes beacon invisibles */
        img[loading="lazy"][src*="reddit.com"][style*="display:none"],
        img[style*="width:0"],
        img[style*="height:0"],
        img[style*="opacity:0"] {
            display: none !important;
        }

        /* Desactivar atributos que podrían usarse para rastreo */
        [data-tracking-id],
        [data-event-tracking],
        [data-impression-id] {
            /* Estos elementos se mantienen pero sus atributos se limpian vía JS */
        }
        </style>
    """.trimIndent()

    /**
     * CSS anti-jank: Desabilita animaciones y transiciones innecesarias.
     * Esto reduce layout shifts y reflows, haciendo el scroll más fluido.
     * 
     * OBJETIVO:
     * - Eliminar animaciones CSS que causan jank
     * - Deshabilitar transiciones largas
     * - Forzar compositing de GPU donde sea posible
     * - Reducir reflows causados por cambios de estilo
     */
    val ANTI_JANK_CSS = """
        <style>
        /* ============================================
           REDUCCIÓN DE ANIMACIONES (Anti-Jank)
           ============================================ */

        /* Deshabilitar transiciones globales que causan jank */
        *,
        *::before,
        *::after {
            animation-duration: 0.01ms !important;
            animation-iteration-count: 1 !important;
            transition-duration: 0.01ms !important;
        }

        /* Permitir solo transiciones de opacidad (eficientes en GPU) */
        [style*="transition"] {
            transition: opacity 0.05s ease-out !important;
        }

        /* ============================================
           OPTIMIZACIONES DE RENDERING
           ============================================ */

        /* Forzar compositing de GPU en elementos que se mueven frecuentemente */
        article,
        .Post,
        [role="article"],
        div[data-testid*="post"],
        [data-testid*="comment"] {
            will-change: contents;
            transform: translateZ(0);
            backface-visibility: hidden;
        }

        /* Optimizar scroll: deshabilitar pointer-events durante scroll */
        html.is-scrolling * {
            pointer-events: none !important;
        }

        /* ============================================
           PREVENCIÓN DE LAYOUT SHIFTS
           ============================================ */

        /* Asegurar que contenedores mantienen dimensiones constantes */
        main,
        [role="main"],
        article,
        [role="article"] {
            contain: layout style paint;
        }

        /* Deshabilitar reflows causados por scripts que cambian el DOM */
        div[class*="Modal"],
        div[class*="modal"],
        div[class*="Popup"],
        div[class*="popup"] {
            contain: strict;
        }

        /* ============================================
           OPTIMIZACIONES DE TEXTO Y VISIBILIDAD
           ============================================ */

        /* Deshabilitar text-indent y text-decoration que fuerzan reflows */
        * {
            text-rendering: optimizeSpeed !important;
        }

        /* Usar font-display: swap para no bloquear rendering */
        @font-face {
            font-display: swap;
        }

        /* ============================================
           SCROLL PERFORMANCE
           ============================================ */

        /* Marcar elementos de scroll como passive listeners */
        div[role="main"],
        main,
        [data-testid="sidebar"] {
            -webkit-overflow-scrolling: touch;
            scroll-behavior: auto !important;
        }

        /* Deshabilitar smooth-scroll que puede causar jank */
        html {
            scroll-behavior: auto !important;
        }
        </style>
    """.trimIndent()

    /**
     * JavaScript que se inyecta para:
     * 1. Vigilar cambios en el DOM
     * 2. Bloquear scripts que se cargan dinámicamente
     * 3. Limpiar atributos de rastreo
     * 4. Registrar intentos de seguridad
     * 5. Optimizar scroll performance
     */
    val BLOCKING_JAVASCRIPT = """
        <script>
        (function() {
            'use strict';

            // ============================================
            // CONFIGURACIÓN Y CONSTANTES
            // ============================================

            // Patrones de URLs que NO se deben ejecutar
            const BLOCKED_SCRIPT_PATTERNS = [
                'google-analytics',
                'googletagmanager',
                'facebook',
                'doubleclick',
                'tracking',
                'hotjar',
                'mixpanel',
                'amplitude',
                'segment.com'
            ];

            // Atributos a limpiar del DOM
            const DANGEROUS_ATTRIBUTES = [
                'data-tracking-id',
                'data-event-tracking',
                'data-impression-id',
                'data-ad-',
                'onclick',
                'onerror'
            ];

            // Flag para control de scroll performance
            let isScrolling = false;
            let scrollTimeout;

            // ============================================
            // 1. BLOQUEO DE SCRIPTS DINÁMICOS
            // ============================================

            /**
             * Intercepta intentos de agregar scripts al DOM.
             * Se ejecuta antes de que el script se cargue.
             */
            const originalAppendChild = Element.prototype.appendChild;
            Element.prototype.appendChild = function(node) {
                if (node.nodeName === 'SCRIPT') {
                    const src = node.getAttribute('src') || '';
                    const content = node.textContent || '';

                    // Verificar si el script tiene patrones bloqueados
                    if (isBlockedScript(src, content)) {
                        console.log('[SECURITY] Blocked dynamic script: ' + src);
                        logSecurityEvent('blocked_script', src);
                        return node;  // Retornar sin ejecutar
                    }
                }
                return originalAppendChild.call(this, node);
            };

            /**
             * Lo mismo para insertBefore (método alternativo)
             */
            const originalInsertBefore = Element.prototype.insertBefore;
            Element.prototype.insertBefore = function(node, ref) {
                if (node.nodeName === 'SCRIPT') {
                    const src = node.getAttribute('src') || '';
                    if (isBlockedScript(src, node.textContent || '')) {
                        console.log('[SECURITY] Blocked dynamic script: ' + src);
                        logSecurityEvent('blocked_script', src);
                        return node;
                    }
                }
                return originalInsertBefore.call(this, node, ref);
            };

            // ============================================
            // 2. VIGILANCIA DE MUTACIONES EN EL DOM
            // ============================================

            /**
             * MutationObserver vigila cambios en el DOM.
             * Se ejecuta cada vez que se agrega/modifica contenido.
             * 
             * NOTA: Esto es importante porque Reddit carga contenido dinámicamente.
             */
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    // Procesar nodos agregados
                    if (mutation.addedNodes.length) {
                        mutation.addedNodes.forEach(function(node) {
                            if (node.nodeType === 1) {  // Solo elementos
                                // Verificar si es un script o iframe bloqueado
                                if (node.nodeName === 'SCRIPT' || node.nodeName === 'IFRAME') {
                                    const src = node.getAttribute('src') || '';
                                    if (isBlockedContent(src)) {
                                        console.log('[SECURITY] Removing element: ' + node.nodeName);
                                        node.remove();
                                        logSecurityEvent('removed_element', node.nodeName);
                                    }
                                }
                                // Limpiar atributos peligrosos
                                cleanDangerousAttributes(node);
                            }
                        });
                    }
                });
            });

            /**
             * Configuración del observador:
             * - childList: detecta cuando se agregan/removen hijos
             * - subtree: observa todo el árbol de descendientes
             * - attributes: detecta cambios de atributos
             */
            observer.observe(document.documentElement, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ['src', 'data-', 'onclick', 'onerror']
            });

            // ============================================
            // 3. OPTIMIZACIÓN DE SCROLL PERFORMANCE
            // ============================================

            /**
             * Marca que se está haciendo scroll para optimizar rendering.
             * Disabilita pointer-events durante scroll para evitar jank.
             */
            document.addEventListener('scroll', function() {
                if (!isScrolling) {
                    document.documentElement.classList.add('is-scrolling');
                    isScrolling = true;
                }
                clearTimeout(scrollTimeout);
                scrollTimeout = setTimeout(function() {
                    document.documentElement.classList.remove('is-scrolling');
                    isScrolling = false;
                }, 150);
            }, { passive: true });

            // ============================================
            // 4. BLOQUEO DE OBSERVADORES AGRESIVOS
            // ============================================

            /**
             * Intercepta MutationObserver para evitar que se ejecuten
             * observadores que pueden causar jank.
             */
            const originalObserverPrototype = MutationObserver.prototype;
            const originalObserve = originalObserverPrototype.observe;

            // Nota: No bloqueamos observadores porque reddit los necesita,
            // pero podríamos limitar su frecuencia si fuera necesario.

            // ============================================
            // 5. FUNCIONES AUXILIARES
            // ============================================

            /**
             * Verifica si un script debe ser bloqueado.
             * 
             * @param {string} src - URL del script
             * @param {string} content - Contenido del script inline
             * @return {boolean} true si debe bloquearse
             */
            function isBlockedScript(src, content) {
                // Bloquear por URL
                if (isBlockedContent(src)) return true;

                // Bloquear por patrones en el contenido
                const suspiciousPatterns = [
                    'google',
                    'tracking',
                    'analytics',
                    'pixel',
                    'beacon'
                ];

                return suspiciousPatterns.some(pattern =>
                    content.toLowerCase().includes(pattern)
                );
            }

            /**
             * Verifica si contenido debe ser bloqueado.
             */
            function isBlockedContent(url) {
                const lowerUrl = url.toLowerCase();
                return BLOCKED_SCRIPT_PATTERNS.some(pattern =>
                    lowerUrl.includes(pattern)
                );
            }

            /**
             * Limpia atributos peligrosos de un elemento.
             * Preserva la estructura del DOM pero elimina contenido malicioso.
             * 
             * @param {Element} element - Elemento a limpiar
             */
            function cleanDangerousAttributes(element) {
                // Procesar el elemento actual
                Array.from(element.attributes || []).forEach(attr => {
                    if (isDangerousAttribute(attr.name)) {
                        element.removeAttribute(attr.name);
                        logSecurityEvent('cleaned_attribute', attr.name);
                    }
                });

                // Procesar todos los descendientes
                element.querySelectorAll('*').forEach(child => {
                    Array.from(child.attributes || []).forEach(attr => {
                        if (isDangerousAttribute(attr.name)) {
                            child.removeAttribute(attr.name);
                        }
                    });
                });
            }

            /**
             * Verifica si un atributo es peligroso.
             */
            function isDangerousAttribute(name) {
                return DANGEROUS_ATTRIBUTES.some(pattern =>
                    name.toLowerCase().startsWith(pattern.toLowerCase())
                );
            }

            /**
             * Registra eventos de seguridad para auditoría.
             * En producción, enviar a servidor de logs.
             */
            function logSecurityEvent(type, details) {
                // Log en consola (solo visible en DevTools)
                console.log('[SECURITY EVENT] ' + type + ': ' + details);

                // TODO: Enviar a servidor de logs seguro
                // fetch('/api/security-logs', {
                //     method: 'POST',
                //     body: JSON.stringify({
                //         type: type,
                //         details: details,
                //         timestamp: new Date().toISOString()
                //     })
                // });
            }

            // ============================================
            // 6. EJECUCIÓN INICIAL
            // ============================================

            // Limpiar el DOM existente cuando se inyecta
            document.documentElement.querySelectorAll('*').forEach(el => {
                cleanDangerousAttributes(el);
            });

            console.log('[SECURITY] DOM Security initialized with anti-jank optimizations');

        })();
        </script>
    """.trimIndent()

    /**
     * Obtiene todo el código CSS necesario para inyectar.
     */
    fun getBlockingCSS(): String = BLOCKING_CSS

    /**
     * Obtiene CSS anti-jank para optimizar scroll performance.
     */
    fun getAntiJankCSS(): String = ANTI_JANK_CSS

    /**
     * Obtiene todo el código JavaScript necesario para inyectar.
     */
    fun getBlockingJavaScript(): String = BLOCKING_JAVASCRIPT

    /**
     * CÓMO AGREGAR NUEVOS FILTROS:
     * 
     * ====== PARA NUEVO DOMINIO DE RASTREO ======
     * 1. Agregar a BLOCKED_SCRIPT_PATTERNS en JavaScript
     * 2. Agregar regla CSS en BLOCKING_CSS para ocultar iframes/imgs
     * 3. Probar exhaustivamente
     * 
     * ====== PARA NUEVO SELECTOR CSS ======
     * 1. Agregar al bloque "BLOQUEADORES" en BLOCKING_CSS
     * 2. Usar selectores específicos (data-testid, class, id)
     * 3. Siempre agregar !important
     * 4. Comentar qué elementos afecta
     * 
     * ====== PARA NUEVA TÉCNICA DE RASTREO ======
     * 1. Identificar el patrón (atributo, evento, etc.)
     * 2. Agregar a DANGEROUS_ATTRIBUTES o crear función especial
     * 3. Llamar a cleanDangerousAttributes() desde el observer
     * 
     * ====== PREVENCIÓN DE REGRESIONES ======
     * Después de cada cambio:
     * 1. Probar que Reddit carga correctamente
     * 2. Verificar que feed de posts es visible
     * 3. Verificar que comentarios se pueden expandir
     * 4. Verificar que se puede upvote/downvote
     * 5. Revisar logs para falsos positivos
     */
}
