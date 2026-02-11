# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. Manter todas as classes de dados da API (DTOs)
# Isso impede que o R8 renomeie os campos que precisam virar JSON
-keep class com.gabrielafonso.ipb.castelobranco.data.api.** { *; }

# 2. Manter a interface da API (para o Retrofit ler os métodos corretamente)
-keep interface com.gabrielafonso.ipb.castelobranco.data.api.BackendApi { *; }

# 3. Regras essenciais para o Kotlinx Serialization funcionar
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

# CONSERVAR OS MODELS (Importante: AuthTokens está aqui!)
-keep class com.gabrielafonso.ipb.castelobranco.domain.model.** { *; }