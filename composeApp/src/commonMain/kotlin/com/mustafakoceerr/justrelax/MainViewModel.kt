package com.mustafakoceerr.justrelax

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
     getAppThemeUseCase: GetAppThemeUseCase,
) : ScreenModel {
    val currentTheme = getAppThemeUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)
}