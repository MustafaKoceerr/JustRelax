package com.mustafakoceerr.justrelax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** App-wide state used by the root composable. */
class MainViewModel(preferences: UserPreferencesRepository) : ViewModel() {

    val theme: StateFlow<AppTheme> = preferences.getTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.SYSTEM)
}
