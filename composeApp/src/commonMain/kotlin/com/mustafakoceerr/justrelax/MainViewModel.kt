package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val getAppThemeUseCase: GetAppThemeUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase
    // Sync UseCase'e burada artık ihtiyacımız yok (Splash/Onboarding hallediyor)
) : ScreenModel {

    // MainActivity'nin dinleyeceği tema akışı
    val currentTheme = getAppThemeUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    // Dil yönetimi
    val currentLanguage = getAppLanguageUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.SYSTEM)
}