package com.mustafakoceerr.justrelax.di

// import com.mustafakoceerr.justrelax.feature.home.HomeViewModel (İleride eklenecek)
import com.mustafakoceerr.justrelax.feature.ai.AiViewModel
import com.mustafakoceerr.justrelax.feature.ai.data.AiService
import com.mustafakoceerr.justrelax.feature.ai.data.AiServiceImpl
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.DownloadAllMissingSoundsUseCase
import com.mustafakoceerr.justrelax.feature.main.MainViewModel
import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.domain.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.saved.SavedViewModel
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.PlaySavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.SaveMixUseCase
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlinx.serialization.json.Json as KotlinJson
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE

val appModule = module {
    // ViewModel'ler UI katmanına (composeApp) aittir.
    // Core modülündeki Repository'leri otomatik olarak bulup kullanırlar.

    viewModelOf(::SettingsViewModel)

    // YENİ: PlayerViewModel Singleton olarak burada
    factoryOf(::PlayerViewModel)
    factoryOf(::TimerViewModel)

    factoryOf(::GenerateRandomMixUseCase)
}


val homeModule = module {
    // 1. UseCase Tanımı
    factoryOf(::DownloadAllMissingSoundsUseCase)

    // 2. ViewModel (Otomatik olarak UseCase'i inject edecek)
    factoryOf(::HomeViewModel)
}


val savedModule = module {
    factoryOf(::PlaySavedMixUseCase) // Yeni UseCase
    factoryOf(::SavedViewModel)
}

val mixerModule = module {
    // 1. Random Mix UseCase (Zaten vardı)
    factoryOf(::GenerateRandomMixUseCase)

    // 2. YENİ: Save Mix UseCase
    // Bunu eklemezsek ViewModel, constructor'ında bu sınıfı bulamaz ve çöker.
    // Koin, bunun ihtiyaç duyduğu 'SavedMixRepository'yi CoreModule'den bulup getirecek.
    factoryOf(::SaveMixUseCase)

    // 3. ViewModel
    // Constructor değişse bile 'factoryOf' kullandığımız için Koin
    // yeni parametreleri (SaveMixUseCase) otomatik olarak enjekte eder.
    factoryOf(::MixerViewModel)
    factoryOf(::MainViewModel)


}

val aiModule = module {
    single {
        HttpClient {
            // 1. JSON Ayarları (Zaten vardı)
            install(ContentNegotiation) {
                json(KotlinJson {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            // 2. LOGGING EKLENTİSİ (YENİ)
            install(Logging) {
                // Logger.SIMPLE -> Logları standart çıktıya (System.out) basar.
                // Android'de Logcat'te "System.out" tag'i altında veya "I/HttpClient" olarak görünür.
                logger = Logger.SIMPLE

                // LogLevel.ALL -> Header, Body, her şeyi gösterir.
                level = LogLevel.ALL
            }
        }
    }

    singleOf(::AiServiceImpl) bind AiService::class
    factoryOf(::PlayAiMixUseCase)
    factoryOf(::AiViewModel)
}

