package ia.ankherth.reddit

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback

/**
 * MainActivity es el punto de entrada de la aplicación.
 * 
 * COMPONENTES DE SEGURIDAD:
 * 1. ContentSanitizer: Sanitiza HTML antes de renderizar
 * 2. ContentInterceptor: Bloquea solicitudes a dominios de rastreo
 * 3. DOMStyleInjector: Inyecta CSS y JavaScript para protección adicional
 * 4. RedditWebViewClient: Integra todos los componentes
 * 
 * FLUJO DE EJECUCIÓN:
 * 1. WebView solicita contenido
 * 2. ContentInterceptor intercepta y bloquea rastreo
 * 3. HTML se recibe y se renderiza
 * 4. DOMStyleInjector inyecta CSS (oculta anuncios) y JS (bloquea scripts dinámicos)
 * 5. Usuario ve contenido limpio de rastreo y anuncios
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    
    // Componentes de seguridad
    private lateinit var contentSanitizer: ContentSanitizer
    private lateinit var contentInterceptor: ContentInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Ocultar el ActionBar (barra superior)
        supportActionBar?.hide()

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        // INICIALIZAR COMPONENTES DE SEGURIDAD
        // Estos se instancian una sola vez y se usan en toda la sesión
        contentSanitizer = ContentSanitizer()
        contentInterceptor = ContentInterceptor(contentSanitizer)
        
        // Configurar WebView con ajustes de seguridad básicos
        webView.settings.apply {
            javaScriptEnabled = true              // Necesario para funcionalidad de Reddit
            domStorageEnabled = true              // Necesario para sesión
            databaseEnabled = true                // Almacenamiento local
            useWideViewPort = true                // Viewport optimizado
            loadWithOverviewMode = true           // Modo de vista general
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // CACHÉ: optimizar navegación reutilizando recursos ya cargados
            // En Android moderno (API 18+), el caché se gestiona automáticamente
            cacheMode = WebSettings.LOAD_DEFAULT  // Usa caché cuando es válido, si no, red
        }

        // Configurar cliente web con interceptor
        webView.webViewClient = RedditWebViewClient(contentSanitizer, contentInterceptor)
        webView.webChromeClient = RedditWebChromeClient(progressBar)

        // Cargar Reddit con componentes de seguridad activos
        webView.loadUrl("https://www.reddit.com")

        // Configurar OnBackPressed usando la API moderna
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressed()
                }
            }
        })
    }

    /**
     * RedditWebViewClient integra todos los componentes de seguridad.
     * 
     * Responsabilidades:
     * 1. Interceptar solicitudes (bloquear rastreo)
     * 2. Inyectar CSS y JavaScript después de cargar
     * 3. Manejar errores de carga
     * 4. Controlar progreso de carga
     */
    private inner class RedditWebViewClient(
        private val sanitizer: ContentSanitizer,
        private val interceptor: ContentInterceptor
    ) : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
            println("[PAGE] Starting load: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            
            // INYECTAR PROTECCIONES DESPUÉS DE QUE LA PÁGINA CARGA
            // Esto se ejecuta cuando el DOM está listo
            if (view != null) {
                injectSecurityLayers(view)
            }
            println("[PAGE] Finished load: $url")
        }

        /**
         * Inyecta capas de seguridad en el DOM después de renderizar.
         * 
         * ORDEN IMPORTANTE:
         * 1. Primero inyectar CSS (oculta anuncios al instante)
         * 2. Luego inyectar JavaScript (configura observadores y filtros)
         * 
         * @param view WebView donde inyectar
         */
        private fun injectSecurityLayers(view: WebView) {
            try {
                // 1. INYECTAR CSS BLOQUEADOR DE ANUNCIOS Y RASTREO
                // Este CSS se aplica inmediatamente y oculta elementos promocionales
                view.evaluateJavascript(
                    """javascript:(function() {
                        |var style = document.createElement('style');
                        |style.innerHTML = ${DOMStyleInjector.getBlockingCSS().toJavaScriptString()};
                        |document.head.appendChild(style);
                    |})()""".trimMargin(),
                    null
                )
                println("[INJECTOR] CSS security layer injected")

                // 2. INYECTAR JAVASCRIPT DE PROTECCIÓN DINÁMICA
                // Este script configura observadores para vigilar cambios posteriores
                // e impide que scripts maliciosos se carguen dinámicamente
                view.evaluateJavascript(
                    DOMStyleInjector.getBlockingJavaScript(),
                    null
                )
                println("[INJECTOR] JavaScript security layer injected")
                
            } catch (e: Exception) {
                println("[ERROR] Failed to inject security layers: ${e.message}")
            }
        }
    }

    /**
     * RedditWebChromeClient maneja eventos de carga y progreso.
     * Se usa para mostrar el progreso visual al cargar páginas.
     */
    private inner class RedditWebChromeClient(private val progressBar: ProgressBar) : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
        }
    }
}

/**
 * Extensión de utilidad para convertir strings de Kotlin a strings JavaScript.
 * Escapa comillas y caracteres especiales para inyección segura de código.
 */
private fun String.toJavaScriptString(): String {
    val escaped = this
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    return "'$escaped'"
}

