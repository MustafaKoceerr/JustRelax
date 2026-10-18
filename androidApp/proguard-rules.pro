# ----------------------------------------------------------------------------------
# 1. General
# ----------------------------------------------------------------------------------
# Line numbers in crash reports (Crashlytics uploads the mapping file).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Annotations of every kind (incl. Kotlin metadata, parameter and type annotations, defaults)
# and generic signatures that libraries read by reflection.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ----------------------------------------------------------------------------------
# 2. kotlinx.serialization (JSON)
# ----------------------------------------------------------------------------------
# Only our own @Serializable classes are kept, so this is not overly broad.
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable <init>(...);
}
# Enum constants keep the names their JSON values match.
-keepclassmembers enum * {
    @kotlinx.serialization.SerialName <fields>;
}

# ----------------------------------------------------------------------------------
# 3. Koin
# ----------------------------------------------------------------------------------
# Only what Koin creates by reflection; the rest of the library can be shrunk.
-dontwarn org.koin.**
# If a "java.lang.NoSuchMethodException: <init>" appears, enable this:
# -keep class * extends org.koin.core.component.KoinComponent

# ----------------------------------------------------------------------------------
# 4. Ktor and network
# ----------------------------------------------------------------------------------
# Only the engine loaders are needed, not all of Ktor.
-keep class io.ktor.client.engine.** { *; }

# OkHttp and Okio under Ktor.
-dontwarn okhttp3.**
-dontwarn okio.**

# ----------------------------------------------------------------------------------
# 5. Media3
# ----------------------------------------------------------------------------------
# Media3 is modular; unused codecs may be removed.
-dontwarn androidx.media3.**
# If release builds play no sound, enable this:
# -keep class androidx.media3.exoplayer.DefaultRenderersFactory { *; }
