# ============================================================
# CUSO Tailor Mobile - R8 / ProGuard Rules
# Package: com.cuso.tailor
# ============================================================

# Keep generic type information, reflection data, and annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ============================================================
# Application Data Models & DTOs (Prevents field renaming)
# ============================================================
# Keep all classes and member variables in the model package intact
-keep class com.cuso.tailor.model.** { *; }
-keepclassmembers class com.cuso.tailor.model.** { *; }

# Keep any classes and members marked with @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ============================================================
# Gson Rules
# ============================================================
# Keep fields using @SerializedName (removed allowobfuscation so names are preserved)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep core Gson library classes and custom type adapters
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Gson TypeToken for runtime generic reflection
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ============================================================
# Retrofit & OkHttp
# ============================================================
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Keep Retrofit interface methods and annotations
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Suppress warnings from OkHttp and Retrofit internal components
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Keep all Retrofit service API interfaces within your app package
-keep interface com.cuso.tailor.** { *; }

# ============================================================
# iText7 Bouncy Castle Warnings
# ============================================================
-dontwarn com.itextpdf.bouncycastle.BouncyCastleFactory
-dontwarn com.itextpdf.bouncycastlefips.BouncyCastleFipsFactory