# ----------------------------------------------------------------------------------
# 1. GENEL AYARLAR
# ----------------------------------------------------------------------------------
# Hata raporlarında satır numaralarını görmek için
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Metadata ve Annotation'ları koru (Kütüphanelerin birbirini tanıması için şart)
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, AnnotationDefault
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Metadata

# ----------------------------------------------------------------------------------
# 2. KOTLINX SERIALIZATION (JSON) - BURASI KALSIN
# ----------------------------------------------------------------------------------
# Bu kurallar "Overly broad" değildir, çünkü sadece @Serializable ile işaretlenmiş
# senin kendi modellerini korur. Bu gereklidir.
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable <init>(...);
}
# Enum field'larının isimlerini koru (JSON value'su ile eşleşmesi için)
-keepclassmembers enum * {
    @kotlinx.serialization.SerialName <fields>;
}

# ----------------------------------------------------------------------------------
# 3. KOIN (Optimize Edildi)
# ----------------------------------------------------------------------------------
# Tüm kütüphaneyi tutmak yerine, sadece reflection ile oluşturulan yapıları tutuyoruz.
-dontwarn org.koin.**
# Eğer "java.lang.NoSuchMethodException: <init>" hatası alırsan burayı açabilirsin:
# -keep class * extends org.koin.core.component.KoinComponent

# ----------------------------------------------------------------------------------
# 4. KTOR & NETWORK (Optimize Edildi)
# ----------------------------------------------------------------------------------
# Tüm Ktor'u tutmaya gerek yok. Sadece motor yükleyicileri.
-keep class io.ktor.client.engine.** { *; }

# OkHttp kullanıyorsan (Ktor altında)
-dontwarn okhttp3.**
-dontwarn okio.**

# ----------------------------------------------------------------------------------
# 5. MEDIA3 & EXOPLAYER (Optimize Edildi)
# ----------------------------------------------------------------------------------
# Media3 çok modülerdir. Kullanılmayan codecler silinsin.
# Sadece temel bileşenleri ve varsa veritabanı işlemlerini koruyalım.
-dontwarn androidx.media3.**
# Eğer release modda ses çalmazsa, şu satırın başındaki '#' işaretini kaldır:
# -keep class androidx.media3.exoplayer.DefaultRenderersFactory { *; }

# ----------------------------------------------------------------------------------
# 6. COIL 3 (Optimize Edildi)
# ----------------------------------------------------------------------------------
# Coil R8 dostudur. Sadece uyarıları kapatalım.
-dontwarn coil3.**

# ----------------------------------------------------------------------------------
# 7. VOYAGER (Navigation)
# ----------------------------------------------------------------------------------
# Voyager ekranları (Screen) reflection ile state saklıyorsa class isimleri lazım olabilir.
-keepnames class * implements cafe.adriel.voyager.core.screen.Screen

# ----------------------------------------------------------------------------------
# 8. OPENAI (Optimize Edildi)
# ----------------------------------------------------------------------------------
# Sadece modelleri korumak yeterli (Serialization kuralları bunu zaten kapsar ama garanti olsun)
-dontwarn com.aallam.openai.**