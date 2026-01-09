package com.mustafakoceerr.justrelax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SyncLanguageWithSystemUseCase
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val syncLanguageWithSystemUseCase: SyncLanguageWithSystemUseCase
) : ViewModel() {

    fun checkSystemLanguage() {
        viewModelScope.launch {
            try {
                syncLanguageWithSystemUseCase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}