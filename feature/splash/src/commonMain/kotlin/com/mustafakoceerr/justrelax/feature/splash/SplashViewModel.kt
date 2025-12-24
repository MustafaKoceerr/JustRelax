package com.mustafakoceerr.justrelax.feature.splash

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.GetAppSetupStatusUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.feature.splash.mvi.SplashEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SplashViewModel(
    private val getAppSetupStatusUseCase: GetAppSetupStatusUseCase,
    private val syncSoundsIfNecessaryUseCase: SyncSoundsIfNecessaryUseCase
) : ScreenModel {

    private val _effect = Channel<SplashEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        startInitialization()
    }

    @OptIn(ExperimentalTime::class)
    private fun startInitialization() {
        screenModelScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val minSplashDuration = 2000L // Animasyonun tadını çıkarsınlar :)

            // 1. Sync İşlemi (Network I/O)
            // İlk açılışta timestamp 0 olduğu için KESİN çalışır.
            // Sonraki açılışlarda süreye bakar.
            try {
                syncSoundsIfNecessaryUseCase()
            } catch (e: Exception) {
                // İnternet yoksa veya hata olursa yutuyoruz.
                // Akış bozulmaz, kullanıcı yönlendirilir.
                // Eğer ilk açılışsa ve hata aldıysa, Onboarding ekranı zaten
                // veri olmadığını görüp "İnternet Yok" hatası verecektir.
            }

            // 2. Kurulum Kontrolü (Disk I/O)
            val isInstalled = getAppSetupStatusUseCase().first()

            // 3. Süreyi Tamamla (UX)
            val elapsedTime = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsedTime < minSplashDuration) {
                delay(minSplashDuration - elapsedTime)
            }

            // 4. Yönlendir
            if (isInstalled) {
                _effect.send(SplashEffect.NavigateToMain)
            } else {
                _effect.send(SplashEffect.NavigateToOnboarding)
            }
        }
    }
}