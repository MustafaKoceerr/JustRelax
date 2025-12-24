package com.mustafakoceerr.justrelax.core.network

/*
Sorumluluk (SRP): Sadece ve sadece HttpClient nesnesini oluşturmak ve yapılandırmak. JSON ayarları, loglama, timeout gibi tüm teknik ağ detayları bu dosyada yaşar. Başka hiçbir dosya bu detayları bilmek zorunda değildir.

 */
/**
 * Uygulama genelinde kullanılacak olan, merkezi olarak yapılandırılmış Ktor HttpClient.
 * 'internal' olması, bu istemcinin sadece :core:network modülü içinden
 * erişilebilir olmasını sağlar.
 */
/**
 * SRP: Bu nesnenin tek sorumluluğu, uygulama genelinde kullanılacak olan
 * HttpClient'ı yapılandırmak ve sağlamaktır.
 *
 * Best Practices:
 * - Timeout: Yavaş ağlarda takılı kalmayı önler.
 * - User-Agent: CDN ve güvenlik duvarları için standart bir kimlik sağlar.
 * - Content Negotiation: Esnek JSON ayrıştırma.
 * - Default Request: Tüm isteklere ortak header ekler.
 * - Logging: Geliştirme sürecini kolaylaştırır.
 */
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


/**
 * SRP: Bu nesnenin tek sorumluluğu, uygulama genelinde kullanılacak olan
 * HttpClient'ı yapılandırmak ve sağlamaktır.
 *
 * Best Practices:
 * - Platform-Agnostic: Motoru (OkHttp/Darwin) 'expect/actual' ile alır.
 * - Ortak Pluginler: Timeout, User-Agent, JSON, Logging gibi tüm platformlarda
 *   aynı çalışacak plugin'ler burada yapılandırılır.
 */
internal val KtorClient = HttpClient(createHttpClientEngine()) {

    // 1. JSON (De)Serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 2. GENEL TIMEOUT (Ktor Seviyesi)
    // Bir isteğin baştan sona tamamlanması için toplam süre.
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000L // 30 saniye
    }

    // 3. USER-AGENT
    // Sunucuya kim olduğumuzu bildirir.
    install(UserAgent) {
        agent = "JustRelax KMP App v1.0"
    }

    // 4. LOGGING
    // Geliştirme sırasında ağ trafiğini görmek için.
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println("KTOR: $message")
            }
        }
        level = LogLevel.HEADERS
    }

    // 5. DEFAULT REQUEST
    // Her isteğe otomatik eklenecek ayarlar.
    defaultRequest {
        header("Accept", "*/*")
    }
}