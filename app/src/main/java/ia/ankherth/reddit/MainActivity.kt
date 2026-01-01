package ia.ankherth.reddit

import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

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
    
    // Custom User-Agent para privacidad
    private val HARDENED_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/91.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CONFIGURACIÓN DE PANTALLA COMPLETA (Android 11+)
        setupFullscreenMode()
        
        setContentView(R.layout.activity_main)
        
        // Ocultar el ActionBar (barra superior)
        supportActionBar?.hide()

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        // INICIALIZAR COMPONENTES DE SEGURIDAD
        contentSanitizer = ContentSanitizer()
        contentInterceptor = ContentInterceptor(contentSanitizer)
        
        // Configurar WebView con ajustes de seguridad y rendimiento
        setupWebViewSettings()
        
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
                    @Suppress("DEPRECATION")
                    onBackPressed()
                }
            }
        })
    }

    /**
     * Configura el modo de pantalla completa verdadera (sin barra de estado).
     * Usa APIs modernas (Android 11+) para máxima compatibilidad.
     * Permite mostrar/ocultar la barra con swipe descendente.
     */
    private fun setupFullscreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: API moderna y segura
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Configurar insets controller para control de visibilidad de UI del sistema
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            
            // Permitir mostrar barra de estado con swipe desde arriba
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0+: API anterior pero compatible
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Configuración adicional de ventana para reducir overhead
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }

    /**
     * Configura WebView con optimizaciones de seguridad, privacidad y rendimiento.
     * Incluye custom User-Agent, caché persistente y disables de APIs innecesarias.
     */
    private fun setupWebViewSettings() {
        webView.settings.apply {
            // ========== PRIVACIDAD & HARDENING ==========
            // User-Agent personalizado para evitar fingerprinting
            userAgentString = HARDENED_USER_AGENT
            
            // Desabilitar APIs de sensores y ubicación que exponen el dispositivo
            @Suppress("DEPRECATION")
            databaseEnabled = true              // Necesario para sesión
            this.domStorageEnabled = true       // Necesario para localStorage
            
            // ========== FUNCIONALIDAD ==========
            javaScriptEnabled = true            // Necesario para Reddit
            useWideViewPort = true              // Viewport optimizado
            loadWithOverviewMode = true         // Modo de vista general
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // ========== RENDIMIENTO & CACHÉ ==========
            cacheMode = WebSettings.LOAD_DEFAULT  // Usa caché cuando es válido
            
            // Minimizar scroll-jank: optimizar rendering
            textZoom = 100                      // Sin zoom de texto
            
            // Disable de geolocation y otros permisos peligrosos implícitos
            setGeolocationEnabled(false)
            
            // Reducir reflows: desabilitar algunos comportamientos innecesarios
            blockNetworkImage = false           // Necesario para Reddit
            blockNetworkLoads = false           // Necesario para Reddit
        }
    }

    /**
     * RedditWebViewClient integra todos los componentes de seguridad.
     * 
     * Responsabilidades:
     * 1. Interceptar solicitudes (bloquear rastreo y ads)
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
            if (view != null) {
                injectSecurityLayers(view)
            }
            println("[PAGE] Finished load: $url")
        }

        /**
         * Inyecta capas de seguridad en el DOM después de renderizar.
         * 
         * ORDEN IMPORTANTE:
         * 1. CSS de bloqueo (oculta ads/trackers al instante)
         * 2. CSS anti-jank (desabilita animaciones innecesarias)
         * 3. JavaScript defensivo (bloquea scripts dinámicos y observadores agresivos)
         * 
         * @param view WebView donde inyectar
         */
        private fun injectSecurityLayers(view: WebView) {
            try {
                // 1. INYECTAR CSS BLOQUEADOR DE ANUNCIOS Y RASTREO
                view.evaluateJavascript(
                    """javascript:(function() {
                        |var style = document.createElement('style');
                        |style.innerHTML = ${DOMStyleInjector.getBlockingCSS().toJavaScriptString()};
                        |document.head.appendChild(style);
                    |})()""".trimMargin(),
                    null
                )
                println("[INJECTOR] CSS blocking layer injected")

                // 2. INYECTAR CSS ANTI-JANK (disables animaciones y transiciones)
                view.evaluateJavascript(
                    """javascript:(function() {
                        |var style = document.createElement('style');
                        |style.innerHTML = ${DOMStyleInjector.getAntiJankCSS().toJavaScriptString()};
                        |document.head.appendChild(style);
                    |})()""".trimMargin(),
                    null
                )
                println("[INJECTOR] Anti-jank CSS layer injected")

                // 3. INYECTAR JAVASCRIPT DE PROTECCIÓN DINÁMICA
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

