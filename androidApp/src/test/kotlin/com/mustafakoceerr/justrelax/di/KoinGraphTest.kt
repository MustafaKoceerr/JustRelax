package com.mustafakoceerr.justrelax.di

import android.content.Context
import android.os.Looper
import app.cash.sqldelight.db.SqlDriver
import com.mustafakoceerr.justrelax.core.database.db.Saved_scene_layer
import com.mustafakoceerr.justrelax.core.database.db.Sound
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
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
        module { includes(appModules + androidPlatformModule + buildTypeModule) }.verify(
            extraTypes = listOf(
                Context::class,
                // *Queries tanımları lambda ile JustRelaxDatabase üzerinden alınır
                SqlDriver::class,
                Sound.Adapter::class,
                Saved_scene_layer.Adapter::class,
                // HttpClient lambda ile hazır KtorClient'tan gelir
                HttpClientEngine::class,
                HttpClientConfig::class,
                // StarterPackInstaller's app-wide scope is created in its definition
                CoroutineScope::class,
                // AudioThread's looper comes from the HandlerThread its definition starts
                Looper::class,
                // SoundEditorViewModel gets its file and origin from the route with parametersOf
                String::class,
                SoundOrigin::class,
            )
        )
    }
}
