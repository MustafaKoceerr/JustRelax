package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/*
Sorumluluk (SRP): Sadece ve sadece HttpClient nesnesini oluşturmak ve yapılandırmak. JSON ayarları, loglama, timeout gibi tüm teknik ağ detayları bu dosyada yaşar. Başka hiçbir dosya bu detayları bilmek zorunda değildir.

 */
/**
 * Uygulama genelinde kullanılacak olan, merkezi olarak yapılandırılmış Ktor HttpClient.
 * 'internal' olması, bu istemcinin sadece :core:network modülü içinden
 * erişilebilir olmasını sağlar.
 */
internal val KtorClient = HttpClient {
    // 1. JSON (De)Serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true // Backend yeni bir alan eklerse uygulamanın çökmesini engeller
        })
    }

    // 2. Logging
    // Geliştirme sırasında ağ isteklerini ve yanıtlarını Logcat/Konsol'da görmek için.
    install(Logging) {
        level = LogLevel.ALL
    }

    // 3. Timeout (Opsiyonel ama önerilir)
    // install(HttpTimeout) {
    //     requestTimeoutMillis = 15000
    //     connectTimeoutMillis = 15000
    //     socketTimeoutMillis = 15000
    // }
}