package com.mustafakoceerr.justrelax.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.mustafakoceerr.justrelax.core.database.db.Sound
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Koin hataları derlemede değil uygulama açılırken patlar. Bu test, tüm modüllerin
 * bağımlılıklarının çözülebildiğini cihaz gerekmeden doğrular.
 */
class KoinGraphTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun allModules_canResolveTheirDependencies() {
        module { includes(appModules + androidPlatformModule) }.verify(
            extraTypes = listOf(
                Context::class,
                // *Queries tanımları lambda ile JustRelaxDatabase üzerinden alınır
                SqlDriver::class,
                Sound.Adapter::class,
                // HttpClient lambda ile hazır KtorClient'tan gelir
                HttpClientEngine::class,
                HttpClientConfig::class,
            )
        )
    }
}
