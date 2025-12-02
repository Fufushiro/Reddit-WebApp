# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# REGLAS DE REDDIT WEBAPP
# ============================================================================

# Preservar clases de la aplicación
-keep class ia.ankherth.reddit.** { *; }

# Preservar enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preservar clases que tienen constructores sin argumentos
-keepclasseswithmembers class * {
    public <init>();
}

# WebView con JavaScript Interface
-keepclassmembers class ia.ankherth.reddit.MainActivity$* {
    public *;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ============================================================================
# DEPENDENCIAS EXTERNAS
# ============================================================================

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# ============================================================================
# DEPURACIÓN Y LOGGING
# ============================================================================

# Preservar información de línea de número para debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# OPTIMIZACIONES
# ============================================================================

# Permitir acceso a clases privadas en proguard
-allowaccessmodification

# Asumir que el main está siendo usado
-keep public class * {
    public static void main(java.lang.String[]);
}
